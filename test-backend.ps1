
# ═══════════════════════════════════════════════════════════════════
#  FULL BACKEND TEST SUITE — Perfume eCommerce
#  Run: powershell -ExecutionPolicy Bypass -File test-backend.ps1
# ═══════════════════════════════════════════════════════════════════

$BASE = "http://localhost:8080/api"
$PASS = 0; $FAIL = 0; $TOTAL = 0

function Test-API {
    param($Name, $Method, $Url, $Body=$null, $Token=$null, $ExpectStatus, $ExpectField=$null, $ExpectValue=$null, $ShouldFail=$false)
    $TOTAL++; $script:TOTAL = $TOTAL
    $headers = @{ "Content-Type" = "application/json" }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    try {
        $params = @{ Uri=$Url; Method=$Method; Headers=$headers; UseBasicParsing=$true; TimeoutSec=10 }
        if ($Body) { $params["Body"] = ($Body | ConvertTo-Json -Compress) }
        $r = Invoke-WebRequest @params
        $status = $r.StatusCode
        $data = $null
        try { $data = $r.Content | ConvertFrom-Json } catch {}

        $ok = ($status -eq $ExpectStatus)
        if ($ok -and $ExpectField -and $data) {
            $actual = $data.$ExpectField
            if ($actual -ne $ExpectValue) { $ok = $false }
        }
        if ($ok) {
            Write-Host "  ✅ PASS  $Name" -ForegroundColor Green
            $script:PASS++
            return $data
        } else {
            Write-Host "  ❌ FAIL  $Name  (got $status, expected $ExpectStatus)" -ForegroundColor Red
            $script:FAIL++
            return $data
        }
    } catch {
        $code = $_.Exception.Response.StatusCode.value__
        if ($ShouldFail -and $code -eq $ExpectStatus) {
            Write-Host "  ✅ PASS  $Name  (correctly rejected with $code)" -ForegroundColor Green
            $script:PASS++
            return $null
        }
        Write-Host "  ❌ FAIL  $Name  (exception: HTTP $code)" -ForegroundColor Red
        $script:FAIL++
        return $null
    }
}

function Section($title) {
    Write-Host "`n$('═'*60)" -ForegroundColor Cyan
    Write-Host "  $title" -ForegroundColor Cyan
    Write-Host "$('═'*60)" -ForegroundColor Cyan
}

# ───────────────────────────────────────────────────────────────────
Section "1. PUBLIC ENDPOINTS (no auth needed)"
# ───────────────────────────────────────────────────────────────────
$products = Test-API "GET /api/products" GET "$BASE/products" -ExpectStatus 200
Write-Host "     → Found $($products.Count) products" -ForegroundColor Gray

$cats = Test-API "GET /api/categories" GET "$BASE/categories" -ExpectStatus 200
Write-Host "     → Found $($cats.Count) categories" -ForegroundColor Gray

if ($products -and $products.Count -gt 0) {
    $pid1 = $products[0].id
    Test-API "GET /api/products/{id}" GET "$BASE/products/$pid1" -ExpectStatus 200 | Out-Null
    Test-API "GET /api/products/category/{id}" GET "$BASE/products/category/1" -ExpectStatus 200 | Out-Null
}

# ───────────────────────────────────────────────────────────────────
Section "2. AUTH — REGISTER & LOGIN"
# ───────────────────────────────────────────────────────────────────
$ts = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$newEmail = "testuser_$ts@test.com"

$regData = Test-API "POST /api/auth/register (new user)" POST "$BASE/auth/register" `
    @{fullName="Test User $ts"; email=$newEmail; password="pass123"; phone="0501234567"; address="123 Test St"} `
    -ExpectStatus 201
$NEW_TOKEN = $regData.token
Write-Host "     → Registered: $newEmail" -ForegroundColor Gray
Write-Host "     → Role: $($regData.role)" -ForegroundColor Gray

Test-API "POST /api/auth/register (duplicate email)" POST "$BASE/auth/register" `
    @{fullName="Dup"; email=$newEmail; password="pass123"; phone="0501234567"; address="test"} `
    -ExpectStatus 400 -ShouldFail $true | Out-Null

$custData = Test-API "POST /api/auth/login (customer)" POST "$BASE/auth/login" `
    @{email="customer@perfume.com"; password="customer123"} -ExpectStatus 200
$CUST_TOKEN = $custData.token
Write-Host "     → Customer token: $($CUST_TOKEN.Substring(0,30))..." -ForegroundColor Gray

$adminData = Test-API "POST /api/auth/login (admin)" POST "$BASE/auth/login" `
    @{email="admin@perfume.com"; password="admin123"} -ExpectStatus 200
$ADMIN_TOKEN = $adminData.token
Write-Host "     → Admin token: $($ADMIN_TOKEN.Substring(0,30))..." -ForegroundColor Gray

Test-API "POST /api/auth/login (wrong password)" POST "$BASE/auth/login" `
    @{email="customer@perfume.com"; password="WRONG"} -ExpectStatus 401 -ShouldFail $true | Out-Null

# ───────────────────────────────────────────────────────────────────
Section "3. CART OPERATIONS (CUSTOMER)"
# ───────────────────────────────────────────────────────────────────
$p1id = $products[0].id
$p2id = if ($products.Count -gt 1) { $products[1].id } else { $products[0].id }

$cart = Test-API "POST /api/cart (add perfume 1, qty=2)" POST "$BASE/cart" `
    @{productId=$p1id; quantity=2} -Token $CUST_TOKEN -ExpectStatus 201
Write-Host "     → Cart total: `$$($cart.totalAmount)" -ForegroundColor Gray

$cart2 = Test-API "POST /api/cart (add perfume 2, qty=1)" POST "$BASE/cart" `
    @{productId=$p2id; quantity=1} -Token $CUST_TOKEN -ExpectStatus 201
Write-Host "     → Cart items: $($cart2.totalItems)" -ForegroundColor Gray

$getCart = Test-API "GET /api/cart" GET "$BASE/cart" -Token $CUST_TOKEN -ExpectStatus 200
Write-Host "     → Cart has $($getCart.totalItems) items, total `$$($getCart.totalAmount)" -ForegroundColor Gray

$itemId = $getCart.items[0].id
$updCart = Test-API "PUT /api/cart/{itemId} (qty=3)" PUT "$BASE/cart/$itemId" `
    @{productId=$p1id; quantity=3} -Token $CUST_TOKEN -ExpectStatus 200
Write-Host "     → Updated qty, new total: `$$($updCart.totalAmount)" -ForegroundColor Gray

# ───────────────────────────────────────────────────────────────────
Section "4. CHECKOUT & ORDERS (CUSTOMER)"
# ───────────────────────────────────────────────────────────────────
$order = Test-API "POST /api/orders/checkout (CREDIT_CARD)" POST "$BASE/orders/checkout" `
    @{shippingAddress="123 Fragrance St, Jeddah"; paymentMethod="CREDIT_CARD"} `
    -Token $CUST_TOKEN -ExpectStatus 201
$ORDER_ID = $order.id
Write-Host "     → Order #$ORDER_ID created" -ForegroundColor Gray
Write-Host "     → Status: $($order.status)" -ForegroundColor Gray
Write-Host "     → Total: `$$($order.totalAmount)" -ForegroundColor Gray
Write-Host "     → Payment: $($order.payment.paymentMethod) | $($order.payment.paymentStatus)" -ForegroundColor Gray
Write-Host "     → Tx Ref: $($order.payment.transactionReference)" -ForegroundColor Gray

$emptyCart = Test-API "GET /api/cart (should be empty after checkout)" GET "$BASE/cart" `
    -Token $CUST_TOKEN -ExpectStatus 200
Write-Host "     → Cart items after checkout: $($emptyCart.totalItems) ✅" -ForegroundColor Gray

$myOrders = Test-API "GET /api/orders/my-orders" GET "$BASE/orders/my-orders" `
    -Token $CUST_TOKEN -ExpectStatus 200
Write-Host "     → Customer has $($myOrders.Count) order(s)" -ForegroundColor Gray

Test-API "GET /api/orders/{id} (own order)" GET "$BASE/orders/$ORDER_ID" `
    -Token $CUST_TOKEN -ExpectStatus 200 | Out-Null

# COD order
Invoke-WebRequest -Uri "$BASE/cart" -Method POST -Body '{"productId":1,"quantity":1}' `
    -Headers @{"Content-Type"="application/json";"Authorization"="Bearer $CUST_TOKEN"} -UseBasicParsing | Out-Null
$codOrder = Test-API "POST /api/orders/checkout (CASH_ON_DELIVERY)" POST "$BASE/orders/checkout" `
    @{shippingAddress="456 Oud Ave, Riyadh"; paymentMethod="CASH_ON_DELIVERY"} `
    -Token $CUST_TOKEN -ExpectStatus 201
Write-Host "     → COD Order #$($codOrder.id) | Payment: $($codOrder.payment.paymentStatus)" -ForegroundColor Gray

# ───────────────────────────────────────────────────────────────────
Section "5. ADMIN — ORDERS"
# ───────────────────────────────────────────────────────────────────
$allOrders = Test-API "GET /api/admin/orders" GET "$BASE/admin/orders" `
    -Token $ADMIN_TOKEN -ExpectStatus 200
Write-Host "     → Total orders in system: $($allOrders.Count)" -ForegroundColor Gray

Test-API "PUT /api/admin/orders/{id}/status → CONFIRMED" PUT "$BASE/admin/orders/$ORDER_ID/status" `
    @{status="CONFIRMED"} -Token $ADMIN_TOKEN -ExpectStatus 200 | Out-Null

Test-API "PUT /api/admin/orders/{id}/status → SHIPPED" PUT "$BASE/admin/orders/$ORDER_ID/status" `
    @{status="SHIPPED"} -Token $ADMIN_TOKEN -ExpectStatus 200 | Out-Null

Test-API "PUT /api/admin/orders/{id}/status → DELIVERED" PUT "$BASE/admin/orders/$ORDER_ID/status" `
    @{status="DELIVERED"} -Token $ADMIN_TOKEN -ExpectStatus 200 | Out-Null

# ───────────────────────────────────────────────────────────────────
Section "6. ADMIN — PRODUCTS CRUD"
# ───────────────────────────────────────────────────────────────────
$newProd = Test-API "POST /api/admin/products (create)" POST "$BASE/admin/products" `
    @{name="Test Oud"; brand="TestBrand"; price=199.99; stockQuantity=30; description="Test perfume"; genderType="UNISEX"; fragranceFamily="WOODY"} `
    -Token $ADMIN_TOKEN -ExpectStatus 201
$NEW_PROD_ID = $newProd.id
Write-Host "     → Created product ID: $NEW_PROD_ID" -ForegroundColor Gray

Test-API "PUT /api/admin/products/{id} (update)" PUT "$BASE/admin/products/$NEW_PROD_ID" `
    @{name="Test Oud Updated"; brand="TestBrand"; price=249.99; stockQuantity=25; description="Updated"} `
    -Token $ADMIN_TOKEN -ExpectStatus 200 | Out-Null

Test-API "DELETE /api/admin/products/{id} (soft delete)" DELETE "$BASE/admin/products/$NEW_PROD_ID" `
    -Token $ADMIN_TOKEN -ExpectStatus 204 | Out-Null

# ───────────────────────────────────────────────────────────────────
Section "7. ADMIN — CATEGORIES CRUD"
# ───────────────────────────────────────────────────────────────────
$newCat = Test-API "POST /api/admin/categories (create)" POST "$BASE/admin/categories" `
    @{name="Test Category $ts"; description="Test desc"} -Token $ADMIN_TOKEN -ExpectStatus 201
$NEW_CAT_ID = $newCat.id
Write-Host "     → Created category ID: $NEW_CAT_ID" -ForegroundColor Gray

Test-API "PUT /api/admin/categories/{id} (update)" PUT "$BASE/admin/categories/$NEW_CAT_ID" `
    @{name="Updated Category $ts"; description="Updated"} -Token $ADMIN_TOKEN -ExpectStatus 200 | Out-Null

Test-API "DELETE /api/admin/categories/{id}" DELETE "$BASE/admin/categories/$NEW_CAT_ID" `
    -Token $ADMIN_TOKEN -ExpectStatus 204 | Out-Null

# ───────────────────────────────────────────────────────────────────
Section "8. ADMIN — USERS"
# ───────────────────────────────────────────────────────────────────
$users = Test-API "GET /api/admin/users" GET "$BASE/admin/users" `
    -Token $ADMIN_TOKEN -ExpectStatus 200
Write-Host "     → Total users: $($users.Count)" -ForegroundColor Gray

Test-API "GET /api/admin/users/{id}" GET "$BASE/admin/users/1" `
    -Token $ADMIN_TOKEN -ExpectStatus 200 | Out-Null

# ───────────────────────────────────────────────────────────────────
Section "9. REVIEWS"
# ───────────────────────────────────────────────────────────────────
Test-API "POST /api/reviews (add review)" POST "$BASE/reviews" `
    @{productId=$p1id; rating=5; comment="Amazing fragrance!"} `
    -Token $CUST_TOKEN -ExpectStatus 201 | Out-Null

$reviews = Test-API "GET /api/reviews/product/{id}" GET "$BASE/reviews/product/$p1id" -ExpectStatus 200
Write-Host "     → Product has $($reviews.Count) review(s)" -ForegroundColor Gray

# ───────────────────────────────────────────────────────────────────
Section "10. REFUNDS"
# ───────────────────────────────────────────────────────────────────
$refund = Test-API "POST /api/refunds (submit)" POST "$BASE/refunds" `
    @{orderId=$ORDER_ID; reason="Item not as described"} -Token $CUST_TOKEN -ExpectStatus 201
$REFUND_ID = $refund.id
Write-Host "     → Refund #$REFUND_ID submitted" -ForegroundColor Gray

Test-API "GET /api/refunds/my-refunds" GET "$BASE/refunds/my-refunds" `
    -Token $CUST_TOKEN -ExpectStatus 200 | Out-Null

$allRefunds = Test-API "GET /api/admin/refunds" GET "$BASE/admin/refunds" `
    -Token $ADMIN_TOKEN -ExpectStatus 200
Write-Host "     → Total refund requests: $($allRefunds.Count)" -ForegroundColor Gray

Test-API "PUT /api/admin/refunds/{id}/status → APPROVED" PUT "$BASE/admin/refunds/$REFUND_ID/status" `
    @{status="APPROVED"; adminNote="Approved by admin"} -Token $ADMIN_TOKEN -ExpectStatus 200 | Out-Null

# ───────────────────────────────────────────────────────────────────
Section "11. SECURITY TESTS"
# ───────────────────────────────────────────────────────────────────
Test-API "No token → GET /api/cart (expect 401)" GET "$BASE/cart" `
    -ExpectStatus 401 -ShouldFail $true | Out-Null

Test-API "Customer → GET /api/admin/orders (expect 403)" GET "$BASE/admin/orders" `
    -Token $CUST_TOKEN -ExpectStatus 403 -ShouldFail $true | Out-Null

Test-API "Admin → GET /api/cart (expect 403)" GET "$BASE/cart" `
    -Token $ADMIN_TOKEN -ExpectStatus 403 -ShouldFail $true | Out-Null

Test-API "Invalid JWT → GET /api/cart (expect 401)" GET "$BASE/cart" `
    -Token "INVALID.TOKEN.XYZ" -ExpectStatus 401 -ShouldFail $true | Out-Null

Test-API "Customer → GET /api/orders/999 (expect 404)" GET "$BASE/orders/999" `
    -Token $CUST_TOKEN -ExpectStatus 404 -ShouldFail $true | Out-Null

Test-API "No token → POST /api/orders/checkout (expect 401)" POST "$BASE/orders/checkout" `
    @{shippingAddress="test"; paymentMethod="CREDIT_CARD"} `
    -ExpectStatus 401 -ShouldFail $true | Out-Null

# ───────────────────────────────────────────────────────────────────
Section "12. VALIDATION TESTS"
# ───────────────────────────────────────────────────────────────────
Test-API "Empty checkout body (expect 400)" POST "$BASE/orders/checkout" `
    @{} -Token $CUST_TOKEN -ExpectStatus 400 -ShouldFail $true | Out-Null

Test-API "Cart qty=0 (expect 400)" POST "$BASE/cart" `
    @{productId=$p1id; quantity=0} -Token $CUST_TOKEN -ExpectStatus 400 -ShouldFail $true | Out-Null

Test-API "Register missing fields (expect 400)" POST "$BASE/auth/register" `
    @{email="bad@test.com"} -ExpectStatus 400 -ShouldFail $true | Out-Null

Test-API "Checkout empty cart (expect 400)" POST "$BASE/orders/checkout" `
    @{shippingAddress="Test Addr"; paymentMethod="CREDIT_CARD"} `
    -Token $CUST_TOKEN -ExpectStatus 400 -ShouldFail $true | Out-Null

# ───────────────────────────────────────────────────────────────────
Section "13. SWAGGER & H2 CONSOLE"
# ───────────────────────────────────────────────────────────────────
try {
    $sw = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui.html" -UseBasicParsing -MaximumRedirection 5 -TimeoutSec 5
    Write-Host "  ✅ PASS  Swagger UI accessible (HTTP $($sw.StatusCode))" -ForegroundColor Green
    $script:PASS++
} catch { Write-Host "  ❌ FAIL  Swagger UI not accessible" -ForegroundColor Red; $script:FAIL++ }
$script:TOTAL++

try {
    $apidocs = Invoke-WebRequest -Uri "http://localhost:8080/v3/api-docs" -UseBasicParsing -TimeoutSec 5
    Write-Host "  ✅ PASS  OpenAPI JSON accessible (HTTP $($apidocs.StatusCode))" -ForegroundColor Green
    $script:PASS++
} catch { Write-Host "  ❌ FAIL  OpenAPI JSON not accessible" -ForegroundColor Red; $script:FAIL++ }
$script:TOTAL++

# ═══════════════════════════════════════════════════════════════════
#  FINAL REPORT
# ═══════════════════════════════════════════════════════════════════
Write-Host "`n$('═'*60)" -ForegroundColor White
Write-Host "  FINAL RESULTS" -ForegroundColor White
Write-Host "$('═'*60)" -ForegroundColor White
Write-Host "  Total Tests : $($script:TOTAL)" -ForegroundColor White
Write-Host "  Passed      : $($script:PASS)  ✅" -ForegroundColor Green
Write-Host "  Failed      : $($script:FAIL)  $(if($script:FAIL -eq 0){'✅'}else{'❌'})" -ForegroundColor $(if($script:FAIL -eq 0){'Green'}else{'Red'})
$pct = [math]::Round(($script:PASS / $script:TOTAL) * 100)
Write-Host "  Score       : $pct%" -ForegroundColor $(if($pct -ge 90){'Green'}elseif($pct -ge 70){'Yellow'}else{'Red'})
Write-Host "$('═'*60)" -ForegroundColor White

if ($script:FAIL -eq 0) {
    Write-Host "`n  🎉 ALL TESTS PASSED — Backend is fully working!" -ForegroundColor Green
} else {
    Write-Host "`n  ⚠️  Some tests failed. Check output above." -ForegroundColor Yellow
}

Write-Host "`n  Browser URLs:" -ForegroundColor Cyan
Write-Host "  🌐 Swagger UI  → http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  🗄️  H2 Console  → http://localhost:8080/h2-console" -ForegroundColor Cyan
Write-Host "  📄 API Docs    → http://localhost:8080/v3/api-docs" -ForegroundColor Cyan
