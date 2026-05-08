package com.example.ecommerence_project.service.impl;

import com.example.ecommerence_project.dto.request.CartItemRequest;
import com.example.ecommerence_project.dto.response.CartResponse;
import com.example.ecommerence_project.entity.Cart;
import com.example.ecommerence_project.entity.CartItem;
import com.example.ecommerence_project.entity.ProductVariant;
import com.example.ecommerence_project.entity.User;
import com.example.ecommerence_project.exception.BadRequestException;
import com.example.ecommerence_project.exception.ResourceNotFoundException;
import com.example.ecommerence_project.exception.UnauthorizedException;
import com.example.ecommerence_project.mapper.CartMapper;
import com.example.ecommerence_project.repository.CartItemRepository;
import com.example.ecommerence_project.repository.CartRepository;
import com.example.ecommerence_project.repository.ProductVariantRepository;
import com.example.ecommerence_project.repository.UserRepository;
import com.example.ecommerence_project.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return cartMapper.toResponse(cart);
    }

    @Override
    public CartResponse addItem(CartItemRequest request, String email) {
        Cart cart = getOrCreateCart(email);

        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

        if (!variant.getActive()) {
            throw new BadRequestException("This product variant is no longer available");
        }
        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + variant.getStockQuantity());
        }

        Optional<CartItem> existing = cartItemRepository
                .findByCartIdAndProductVariantId(cart.getId(), variant.getId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (variant.getStockQuantity() < newQty) {
                throw new BadRequestException("Insufficient stock. Available: " + variant.getStockQuantity());
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(request.getQuantity())
                    .unitPrice(variant.getPrice())
                    .build();
            cartItemRepository.save(item);
        }

        return cartMapper.toResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Override
    public CartResponse updateItem(Long cartItemId, Integer quantity, String email) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        validateCartOwnership(item.getCart(), email);

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            if (item.getProductVariant().getStockQuantity() < quantity) {
                throw new BadRequestException("Insufficient stock. Available: " + item.getProductVariant().getStockQuantity());
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        Cart cart = cartRepository.findById(item.getCart().getId()).orElseThrow();
        return cartMapper.toResponse(cart);
    }

    @Override
    public CartResponse removeItem(Long cartItemId, String email) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        validateCartOwnership(item.getCart(), email);
        Long cartId = item.getCart().getId();
        cartItemRepository.delete(item);

        Cart cart = cartRepository.findById(cartId).orElseThrow();
        return cartMapper.toResponse(cart);
    }

    @Override
    public void clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private void validateCartOwnership(Cart cart, String email) {
        if (!cart.getUser().getEmail().equals(email)) {
            throw new UnauthorizedException("You do not have access to this cart item");
        }
    }
}
