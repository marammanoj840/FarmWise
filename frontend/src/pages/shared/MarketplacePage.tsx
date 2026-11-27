import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import {
  CheckCircle2,
  Info,
  Package,
  Search,
  ShoppingBag,
  ShoppingCart,
  Trash2,
  Wheat,
} from "lucide-react";
import { marketplaceApi } from "@/api/services";
import { useAuth } from "@/context/AuthContext";
import { useToast } from "@/context/ToastContext";
import { extractErrorMessage, formatCurrencyINR, formatDate } from "@/utils/format";
import type { ProduceListing, Product } from "@/types/api";
import { Badge, Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { Drawer, Modal } from "@/components/ui/Modal";
import { Breadcrumbs, EmptyState, Skeleton } from "@/components/ui/Feedback";

export function MarketplacePage() {
  const { isAuthenticated } = useAuth();
  const { success, error } = useToast();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [tab, setTab] = useState<"inputs" | "produce" | "orders">("inputs");
  const [categoryId, setCategoryId] = useState("");
  const [search, setSearch] = useState("");
  const [sort, setSort] = useState("newest");
  const [detailProduct, setDetailProduct] = useState<Product | null>(null);
  const [cartOpen, setCartOpen] = useState(false);
  const [checkoutOpen, setCheckoutOpen] = useState(false);
  const [inquiryTarget, setInquiryTarget] = useState<ProduceListing | null>(null);

  const { data: categories } = useQuery({ queryKey: ["marketplace", "categories"], queryFn: marketplaceApi.categories });
  const { data: products, isLoading: prodLoading } = useQuery({
    queryKey: ["marketplace", "products", categoryId, search, sort],
    queryFn: () =>
      marketplaceApi.products({
        categoryId: categoryId || undefined,
        q: search || undefined,
        sort,
        page: 0,
        size: 30,
      }),
  });
  const { data: produce } = useQuery({
    queryKey: ["marketplace", "produce", search],
    queryFn: () => marketplaceApi.listProduce(search || undefined, 0, 30),
  });
  const { data: cart } = useQuery({
    queryKey: ["marketplace", "cart"],
    queryFn: marketplaceApi.getCart,
    enabled: isAuthenticated,
  });
  const { data: orders } = useQuery({
    queryKey: ["marketplace", "orders"],
    queryFn: () => marketplaceApi.myOrders(0, 20),
    enabled: isAuthenticated && tab === "orders",
  });

  const checkoutForm = useForm({
    defaultValues: {
      shippingName: "",
      shippingPhone: "",
      shippingAddress: "",
      shippingCity: "",
      shippingState: "",
      shippingPostalCode: "",
      notes: "",
    },
  });

  const inquiryForm = useForm({
    defaultValues: {
      offeredPriceInr: "",
      requestedQuantity: 5,
      message: "",
    },
  });

  const addToCart = useMutation({
    mutationFn: ({ id, qty }: { id: string; qty: number }) => marketplaceApi.addToCart(id, qty),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["marketplace", "cart"] });
      success("Added to cart");
    },
    onError: (err) => error("Could not add to cart", extractErrorMessage(err)),
  });

  const updateCartItem = useMutation({
    mutationFn: ({ id, qty }: { id: string; qty: number }) => marketplaceApi.updateCartItem(id, qty),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["marketplace", "cart"] }),
    onError: (err) => error("Cart update failed", extractErrorMessage(err)),
  });

  const removeCartItem = useMutation({
    mutationFn: marketplaceApi.removeCartItem,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["marketplace", "cart"] }),
  });

  const checkoutMutation = useMutation({
    mutationFn: (v: Record<string, unknown>) => marketplaceApi.checkout(v),
    onSuccess: (order) => {
      queryClient.invalidateQueries({ queryKey: ["marketplace", "cart"] });
      queryClient.invalidateQueries({ queryKey: ["marketplace", "orders"] });
      queryClient.invalidateQueries({ queryKey: ["marketplace", "products"] });
      setCheckoutOpen(false);
      setCartOpen(false);
      setTab("orders");
      success(`Order ${order.orderNumber} recorded`, order.paymentNotice);
    },
    onError: (err) => error("Checkout failed", extractErrorMessage(err)),
  });

  const inquiryMutation = useMutation({
    mutationFn: (v: Record<string, unknown>) =>
      marketplaceApi.submitProduceInquiry(inquiryTarget!.id, {
        offeredPriceInr: v.offeredPriceInr ? Number(v.offeredPriceInr) : null,
        requestedQuantity: Number(v.requestedQuantity),
        message: v.message,
      }),
    onSuccess: () => {
      setInquiryTarget(null);
      inquiryForm.reset();
      success("Inquiry submitted to farmer");
    },
    onError: (err) => error("Inquiry failed", extractErrorMessage(err)),
  });

  const handleAddClick = (product: Product) => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/marketplace");
      return;
    }
    addToCart.mutate({ id: product.id, qty: 1 });
  };

  return (
    <div className="mx-auto max-w-7xl space-y-6 px-4 py-8 sm:px-6 lg:px-8">
      <Breadcrumbs items={[{ label: "Agricultural Marketplace" }]} />

      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">FarmWise Agricultural Marketplace</h1>
          <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">
            Shop seeds, fertilizers, crop protection, irrigation kits, or connect directly on farmer produce lots.
          </p>
        </div>

        {isAuthenticated && (
          <Button variant="secondary" onClick={() => setCartOpen(true)} icon={<ShoppingCart className="h-4 w-4" />}>
            Cart ({cart?.totalItems ?? 0})
          </Button>
        )}
      </div>

      <div className="flex flex-wrap gap-2 border-b border-[#DCE5DC] pb-2 dark:border-[#203D31]">
        <button
          type="button"
          onClick={() => setTab("inputs")}
          className={`rounded-xl px-4 py-2 text-sm font-semibold ${
            tab === "inputs" ? "bg-[#10402E] text-white dark:bg-[#2A8C55]" : "text-[#4B6358] hover:bg-[#EEF3EE]"
          }`}
        >
          Agri Inputs & Equipment
        </button>
        <button
          type="button"
          onClick={() => setTab("produce")}
          className={`rounded-xl px-4 py-2 text-sm font-semibold ${
            tab === "produce" ? "bg-[#10402E] text-white dark:bg-[#2A8C55]" : "text-[#4B6358] hover:bg-[#EEF3EE]"
          }`}
        >
          Farmer Produce Listings
        </button>
        {isAuthenticated && (
          <button
            type="button"
            onClick={() => setTab("orders")}
            className={`rounded-xl px-4 py-2 text-sm font-semibold ${
              tab === "orders" ? "bg-[#10402E] text-white dark:bg-[#2A8C55]" : "text-[#4B6358] hover:bg-[#EEF3EE]"
            }`}
          >
            My Orders
          </button>
        )}
      </div>

      {tab === "inputs" && (
        <>
          <div className="grid gap-3 md:grid-cols-12">
            <div className="relative md:col-span-6">
              <Search className="pointer-events-none absolute left-3.5 top-3.5 h-4 w-4 text-[#6B8277]" />
              <input
                type="search"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search seeds, drip kits, sprayers, fertilizers..."
                className="min-h-11 w-full rounded-xl border border-[#C9D6CA] bg-white pl-10 pr-4 text-sm dark:border-[#244337] dark:bg-[#0D1E17]"
              />
            </div>
            <div className="md:col-span-3">
              <Select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
                <option value="">All Categories</option>
                {(categories || []).map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </Select>
            </div>
            <div className="md:col-span-3">
              <Select value={sort} onChange={(e) => setSort(e.target.value)}>
                <option value="newest">Sort: Newest</option>
                <option value="price_asc">Price: Low to High</option>
                <option value="price_desc">Price: High to Low</option>
                <option value="name">Name: A-Z</option>
              </Select>
            </div>
          </div>

          {prodLoading ? (
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
              <Skeleton className="h-72" />
              <Skeleton className="h-72" />
              <Skeleton className="h-72" />
            </div>
          ) : !products || products.items.length === 0 ? (
            <EmptyState
              icon={<Package className="h-6 w-6" />}
              title="No matching products"
              description="Try clearing category or search filters."
            />
          ) : (
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {products.items.map((p) => (
                <Card key={p.id} padding="p-0" className="flex flex-col overflow-hidden">
                  {p.imageUrl ? (
                    <img src={p.imageUrl} alt={p.name} className="h-44 w-full object-cover" loading="lazy" />
                  ) : (
                    <div className="flex h-44 items-center justify-center bg-[#EEF3EE] dark:bg-[#152B22]">
                      <ShoppingBag className="h-10 w-10 text-[#2A8C55]" />
                    </div>
                  )}
                  <div className="flex flex-1 flex-col justify-between p-5">
                    <div>
                      <div className="flex items-center justify-between gap-2 text-xs">
                        <Badge tone="green">{p.categoryName}</Badge>
                        {p.supplierVerified && (
                          <span className="inline-flex items-center gap-1 font-medium text-emerald-700 dark:text-emerald-400">
                            <CheckCircle2 className="h-3.5 w-3.5" /> Verified Supplier
                          </span>
                        )}
                      </div>
                      <h2 className="mt-2.5 text-base font-bold">{p.name}</h2>
                      <p className="mt-1 text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                        {p.supplierName} {p.brand ? `• ${p.brand}` : ""}
                      </p>
                      <p className="mt-2 line-clamp-2 text-xs text-[#4B6358] dark:text-[#9EB7AB]">{p.description}</p>
                    </div>

                    <div className="mt-4 flex items-center justify-between border-t border-[#E9EFE9] pt-3 dark:border-[#1D392E]">
                      <div>
                        <p className="font-display text-xl font-bold">{formatCurrencyINR(p.priceInr)}</p>
                        <p className="text-[11px] text-[#4B6358]">
                          per {p.unit} • {p.stockQuantity} in stock
                        </p>
                      </div>
                      <div className="flex gap-1.5">
                        <Button size="sm" variant="ghost" onClick={() => setDetailProduct(p)}>
                          Details
                        </Button>
                        <Button
                          size="sm"
                          disabled={p.stockQuantity <= 0}
                          onClick={() => handleAddClick(p)}
                        >
                          Add
                        </Button>
                      </div>
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </>
      )}

      {tab === "produce" && (
        <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
          {(produce?.items || []).map((l) => (
            <Card key={l.id} className="flex flex-col justify-between space-y-3">
              <div>
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <h2 className="text-lg font-bold">
                      {l.cropName} {l.variety ? `(${l.variety})` : ""}
                    </h2>
                    <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                      Seller: {l.farmerName} • {l.location}
                    </p>
                  </div>
                  <Badge tone="green">{l.qualityGrade}</Badge>
                </div>
                <div className="mt-3 grid grid-cols-2 gap-2 rounded-xl bg-[#EEF3EE] p-3 text-xs dark:bg-[#152B22]">
                  <div>
                    <span className="text-[#4B6358]">Available Qty</span>
                    <p className="font-bold">
                      {l.quantity} {l.unit}
                    </p>
                  </div>
                  <div>
                    <span className="text-[#4B6358]">Expected Price</span>
                    <p className="font-bold">
                      {formatCurrencyINR(l.expectedPriceInr)}/{l.unit.toLowerCase()}
                    </p>
                  </div>
                </div>
                {l.qualityNotes && <p className="mt-2 text-xs text-[#4B6358] dark:text-[#9EB7AB]">{l.qualityNotes}</p>}
              </div>
              <Button
                size="sm"
                onClick={() => {
                  if (!isAuthenticated) {
                    navigate("/login?redirect=/marketplace");
                  } else {
                    setInquiryTarget(l);
                  }
                }}
                icon={<Wheat className="h-4 w-4" />}
              >
                Submit Buyer Inquiry
              </Button>
            </Card>
          ))}
        </div>
      )}

      {tab === "orders" && (
        <div className="space-y-4">
          {(orders?.items || []).map((o) => (
            <Card key={o.id} className="space-y-3">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <h3 className="font-bold">{o.orderNumber}</h3>
                  <p className="text-xs text-[#4B6358] dark:text-[#9EB7AB]">
                    Placed {formatDate(o.createdAt)} • Ship to {o.shippingCity}, {o.shippingState}
                  </p>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Badge tone="amber">{o.status}</Badge>
                  <Badge tone="slate">Payment: {o.paymentStatus}</Badge>
                  <span className="font-display text-lg font-bold">{formatCurrencyINR(o.totalAmountInr)}</span>
                </div>
              </div>
              <p className="rounded-xl bg-[#EEF3EE] p-2.5 text-xs dark:bg-[#152B22]">{o.paymentNotice}</p>
              <div className="space-y-1.5 border-t border-[#E9EFE9] pt-2 text-xs dark:border-[#1D392E]">
                {o.items.map((oi) => (
                  <div key={oi.id} className="flex items-center justify-between">
                    <span>
                      {oi.productName} × {oi.quantity} ({oi.unit})
                    </span>
                    <span className="font-semibold">
                      {formatCurrencyINR(oi.lineTotalInr)} • {oi.fulfillmentStatus}
                    </span>
                  </div>
                ))}
              </div>
            </Card>
          ))}
          {(!orders || orders.items.length === 0) && (
            <EmptyState title="No orders yet" description="Orders created from your cart appear here." />
          )}
        </div>
      )}

      {/* Product Detail Modal */}
      <Modal open={Boolean(detailProduct)} onClose={() => setDetailProduct(null)} title={detailProduct?.name || ""}>
        {detailProduct && (
          <div className="space-y-4 text-sm">
            {detailProduct.imageUrl && (
              <img src={detailProduct.imageUrl} alt={detailProduct.name} className="h-56 w-full rounded-xl object-cover" />
            )}
            <div className="flex items-center justify-between">
              <Badge tone="green">{detailProduct.categoryName}</Badge>
              <span className="font-display text-2xl font-bold">
                {formatCurrencyINR(detailProduct.priceInr)} / {detailProduct.unit}
              </span>
            </div>
            <p className="text-[#4B6358] dark:text-[#9EB7AB]">{detailProduct.description}</p>
            <p className="text-xs">
              <strong>Supplier:</strong> {detailProduct.supplierName} •{" "}
              {detailProduct.supplierVerified ? "Verified Supplier" : "Verification Pending"}
            </p>
            <Button
              className="w-full"
              onClick={() => {
                handleAddClick(detailProduct);
                setDetailProduct(null);
              }}
            >
              Add to Cart
            </Button>
          </div>
        )}
      </Modal>

      {/* Cart Drawer */}
      <Drawer open={cartOpen} onClose={() => setCartOpen(false)} title="Shopping Cart">
        <div className="flex h-full flex-col justify-between gap-4">
          <div className="space-y-3">
            {cart && !cart.paymentGatewayConfigured && (
              <div className="flex items-start gap-2 rounded-xl border border-amber-200 bg-amber-50 p-3 text-xs text-amber-950 dark:border-amber-900 dark:bg-amber-950/30 dark:text-amber-200">
                <Info className="mt-0.5 h-4 w-4 shrink-0" />
                <span>
                  Payment gateway is currently unconfigured. Checking out records your order cleanly for supplier
                  confirmation without processing online card/UPI payments.
                </span>
              </div>
            )}
            {(cart?.items || []).map((ci) => (
              <div key={ci.id} className="flex items-center justify-between gap-3 rounded-xl border border-[#DCE5DC] p-3 text-xs dark:border-[#203D31]">
                <div className="min-w-0 flex-1">
                  <p className="truncate font-bold">{ci.productName}</p>
                  <p className="text-[#4B6358] dark:text-[#9EB7AB]">
                    {formatCurrencyINR(ci.unitPriceInr)} × {ci.quantity} ={" "}
                    <strong>{formatCurrencyINR(ci.lineTotalInr)}</strong>
                  </p>
                </div>
                <div className="flex items-center gap-1.5">
                  <button
                    type="button"
                    onClick={() =>
                      ci.quantity > 1
                        ? updateCartItem.mutate({ id: ci.id, qty: ci.quantity - 1 })
                        : removeCartItem.mutate(ci.id)
                    }
                    className="h-7 w-7 rounded-lg border font-bold"
                  >
                    -
                  </button>
                  <span className="w-5 text-center font-semibold">{ci.quantity}</span>
                  <button
                    type="button"
                    onClick={() => updateCartItem.mutate({ id: ci.id, qty: ci.quantity + 1 })}
                    className="h-7 w-7 rounded-lg border font-bold"
                  >
                    +
                  </button>
                  <button
                    type="button"
                    onClick={() => removeCartItem.mutate(ci.id)}
                    aria-label="Remove item"
                    className="p-1 text-red-600"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))}
            {(!cart || cart.items.length === 0) && (
              <p className="text-sm text-[#4B6358] dark:text-[#9EB7AB]">Your shopping cart is empty.</p>
            )}
          </div>

          {cart && cart.items.length > 0 && (
            <div className="space-y-3 border-t border-[#DCE5DC] pt-4 dark:border-[#203D31]">
              <div className="flex items-center justify-between text-base font-bold">
                <span>Subtotal (INR)</span>
                <span>{formatCurrencyINR(cart.subtotalInr)}</span>
              </div>
              <Button className="w-full" onClick={() => setCheckoutOpen(true)}>
                Proceed to Order Details
              </Button>
            </div>
          )}
        </div>
      </Drawer>

      {/* Checkout Modal */}
      <Modal open={checkoutOpen} onClose={() => setCheckoutOpen(false)} title="Confirm Order & Delivery Details">
        <form onSubmit={checkoutForm.handleSubmit((v) => checkoutMutation.mutate(v))} className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Recipient Name" required {...checkoutForm.register("shippingName", { required: true })} />
            <Input label="Phone Number" required {...checkoutForm.register("shippingPhone", { required: true })} />
          </div>
          <Textarea label="Delivery Address" required {...checkoutForm.register("shippingAddress", { required: true })} />
          <div className="grid gap-4 sm:grid-cols-3">
            <Input label="City" required {...checkoutForm.register("shippingCity", { required: true })} />
            <Input label="State" required {...checkoutForm.register("shippingState", { required: true })} />
            <Input label="PIN Code" required {...checkoutForm.register("shippingPostalCode", { required: true })} />
          </div>
          <Input label="Order Notes (Optional)" {...checkoutForm.register("notes")} />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setCheckoutOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" loading={checkoutMutation.isPending}>
              Place Order ({formatCurrencyINR(cart?.subtotalInr)})
            </Button>
          </div>
        </form>
      </Modal>

      {/* Buyer Produce Inquiry Modal */}
      <Modal
        open={Boolean(inquiryTarget)}
        onClose={() => setInquiryTarget(null)}
        title={`Inquire About ${inquiryTarget?.cropName || ""}`}
      >
        <form onSubmit={inquiryForm.handleSubmit((v) => inquiryMutation.mutate(v))} className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label={`Requested Quantity (${inquiryTarget?.unit || ""})`}
              type="number"
              step="0.01"
              required
              {...inquiryForm.register("requestedQuantity", { required: true })}
            />
            <Input
              label="Offered Price (INR ₹, optional)"
              type="number"
              step="0.01"
              {...inquiryForm.register("offeredPriceInr")}
            />
          </div>
          <Textarea
            label="Message to Farmer"
            placeholder="Share pickup schedule, packaging expectations, or quality questions..."
            required
            {...inquiryForm.register("message", { required: true })}
          />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setInquiryTarget(null)}>
              Cancel
            </Button>
            <Button type="submit" loading={inquiryMutation.isPending}>
              Send Inquiry
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
