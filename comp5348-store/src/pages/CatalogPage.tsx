import { useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import apiClient from '../api/client';
import type { OrderRequest, OrderResponse, ProductStockResponse } from '../api/types';
import { useAuth } from '../context/AuthContext';
import { useOrders } from '../context/OrdersContext';

const formatter = new Intl.NumberFormat('en-AU', {
  style: 'currency',
  currency: 'AUD',
});

const CatalogPage = () => {
  const { user, token } = useAuth();
  const { addOrder } = useOrders();

  const [products, setProducts] = useState<ProductStockResponse[]>([]);
  const [loadingStock, setLoadingStock] = useState(false);
  const [submittingProductId, setSubmittingProductId] = useState<number | null>(null);
  const [quantities, setQuantities] = useState<Record<number, number>>({});
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const productsWithStock = useMemo(
    () =>
      products.map((product) => ({
        ...product,
        stock: product.totalQuantity,
      })),
    [products],
  );

  const fetchAvailability = async () => {
    setLoadingStock(true);
    setError(null);
    try {
      const { data } = await apiClient.get<ProductStockResponse[]>(
        '/api/products/stock',
      );
      setProducts(data);
    } catch (requestError) {
      setError('Unable to load product stock at the moment.');
    } finally {
      setLoadingStock(false);
    }
  };

  useEffect(() => {
    fetchAvailability();
  }, []);

  const handleQuantityChange = (productId: number, next: number) => {
    setQuantities((prev) => ({
      ...prev,
      [productId]: Number.isNaN(next) || next < 1 ? 1 : Math.floor(next),
    }));
  };

  const placeOrder = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!user || !token || submittingProductId !== null) {
      return;
    }
    const formData = new FormData(event.currentTarget);
    const productId = Number(formData.get('productId'));
    const quantity = Number(formData.get('quantity'));
    const product = products.find((item) => item.productId === productId);
    if (!product) {
      setError('Unknown product selected');
      return;
    }
    if (!Number.isFinite(quantity) || quantity < 1) {
      setError('Quantity must be at least 1');
      return;
    }

    setSubmittingProductId(productId);
    setMessage(null);
    setError(null);

    const totalAmount = Number((product.price * quantity).toFixed(2));
    const payload: OrderRequest = {
      username: user.username,
      productId: product.productId,
      quantity,
      totalAmount,
    };

    try {
      const { data } = await apiClient.post<OrderResponse>(
        '/api/orders',
        payload,
      );
      addOrder(data);
      setQuantities((prev) => ({ ...prev, [product.productId]: 1 }));
      setMessage(
        `Order ${data.orderId} created successfully for ${quantity} x ${product.productName}.`,
      );
      fetchAvailability();
    } catch (requestError: unknown) {
      if (
        requestError &&
        typeof requestError === 'object' &&
        'response' in requestError
      ) {
        const response = (requestError as any).response;
        const messageFromServer =
          response?.data?.message ??
          response?.data ??
          'Unable to place order right now.';
        setError(String(messageFromServer));
      } else {
        setError('Unable to place order right now.');
      }
    } finally {
      setSubmittingProductId(null);
    }
  };

  return (
    <section className="card">
      <header className="card-header">
        <div>
          <h1>Product catalogue</h1>
          <p className="muted">
            Browse the sample products seeded in the store service.
          </p>
        </div>
        <button
          type="button"
          className="btn ghost"
          onClick={fetchAvailability}
          disabled={loadingStock}
        >
          {loadingStock ? 'Refreshing...' : 'Refresh stock'}
        </button>
      </header>

      {message ? <div className="alert success">{message}</div> : null}
      {error ? <div className="alert error">{error}</div> : null}

      <div className="product-grid">
        {productsWithStock.map((product) => {
          const outOfStock = (product.stock ?? 0) <= 0;
          return (
            <article key={product.productId} className="product-card">
              <h2>{product.productName}</h2>
              <p className="price">{formatter.format(product.price)}</p>
              <p className="stock">
                Stock:{' '}
                {product.stock == null
                  ? 'Unknown'
                  : product.stock > 0
                    ? product.stock
                    : 'Out of stock'}
              </p>
              {product.warehouses.length > 0 ? (
                <ul className="warehouse-list">
                  {product.warehouses.map((warehouse) => (
                    <li key={warehouse.warehouseId}>
                      <span>{warehouse.warehouseName}</span>
                      <strong>{warehouse.quantity}</strong>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="muted">No warehouse stock recorded.</p>
              )}
              <form className="order-form" onSubmit={placeOrder}>
                <input
                  type="hidden"
                  name="productId"
                  value={product.productId}
                />
                <label className="form-field">
                  <span>Quantity</span>
                  <input
                    type="number"
                    name="quantity"
                    min={1}
                    max={Math.max(product.stock ?? 1, 1)}
                    value={quantities[product.productId] ?? 1}
                    onChange={(event) =>
                      handleQuantityChange(
                        product.productId,
                        Number(event.currentTarget.value),
                      )
                    }
                  />
                </label>
                <button
                  type="submit"
                  className="btn primary"
                  disabled={
                    !user ||
                    !token ||
                    submittingProductId === product.productId ||
                    outOfStock
                  }
                  title={
                    user
                      ? outOfStock
                        ? 'Product currently unavailable.'
                        : undefined
                      : 'Log in to place an order with the store service.'
                  }
                >
                  {submittingProductId === product.productId
                    ? 'Submitting...'
                    : 'Place order'}
                </button>
              </form>
            </article>
          );
        })}
      </div>
      {!user ? (
        <p className="muted">
          Log in or register to place orders. Catalogue data is public.
        </p>
      ) : null}
      {productsWithStock.length === 0 && !loadingStock && !error ? (
        <p className="muted">
          No products found in the catalogue. Seed data should populate the
          store database.
        </p>
      ) : null}
    </section>
  );
};

export default CatalogPage;
