import { useMemo, useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import { useOrders } from '../context/OrdersContext';
import { useAuth } from '../context/AuthContext';
import type { OrderResponse } from '../api/types';

const formatter = new Intl.DateTimeFormat('en-AU', {
  dateStyle: 'medium',
  timeStyle: 'short',
});

const formatTransactionId = (transactionId?: string | null) => {
  if (!transactionId) {
    return 'Pending';
  }
  if (transactionId.length <= 10) {
    return transactionId;
  }
  return `${transactionId.slice(0, 10)}…`;
};

const OrdersPage = () => {
  const { user, token } = useAuth();
  const { orders, fetchOrder, requestRefund, fetchOrdersForUser } = useOrders();

  const [lookupId, setLookupId] = useState('');
  const [lookupStatus, setLookupStatus] = useState<string | null>(null);
  const [lookupError, setLookupError] = useState<string | null>(null);
  const [processingOrderId, setProcessingOrderId] = useState<number | null>(
    null,
  );

  const sortedOrders = useMemo(
    () =>
      [...orders].sort((a, b) => {
        if (!a.createdAt || !b.createdAt) {
          return 0;
        }
        return b.createdAt.localeCompare(a.createdAt);
      }),
    [orders],
  );

  useEffect(() => {
    // Wait for both user and token to be available to avoid 403 due to missing Authorization header.
    if (!user || !token) return;
    setLookupError(null);
    (async () => {
      try {
        await fetchOrdersForUser(user.userId);
      } catch (err: any) {
        // Try to extract a helpful message from axios error response if present
        const serverMessage =
          err?.response?.data?.message ??
          err?.response?.data?.error ??
          (err?.response?.data ? JSON.stringify(err.response.data) : null);
        setLookupError(serverMessage ?? err?.message ?? 'Failed to load orders.');
      }
    })();
  }, [user, token, fetchOrdersForUser]);

  const handleLookup = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLookupStatus(null);
    setLookupError(null);
    const parsedId = Number(lookupId);
    if (!Number.isFinite(parsedId) || parsedId < 1) {
      setLookupError('Enter a valid order ID.');
      return;
    }
    const result = await fetchOrder(parsedId);
    if (result) {
      setLookupStatus(`Order ${parsedId} loaded.`);
      setLookupId('');
    } else {
      setLookupError('Could not find that order for your account.');
    }
  };

  const handleRefund = async (order: OrderResponse) => {
    setProcessingOrderId(order.orderId);
    setLookupError(null);
    try {
      await requestRefund(order.orderId);
      setLookupStatus(`Refund requested for order ${order.orderId}.`);
    } catch (error) {
      if (error && typeof error === 'object' && 'response' in error) {
        const message =
          (error as any).response?.data?.message ??
          (error as any).response?.data ??
          'Refund request failed.';
        setLookupError(String(message));
      } else {
        setLookupError('Refund request failed.');
      }
    } finally {
      setProcessingOrderId(null);
    }
  };

  if (!user) {
    return null;
  }

  return (
    <section className="card">
      <header className="card-header">
        <div>
          <h1>Your orders</h1>
          <p className="muted">
            Add recent orders by ID or refund an existing purchase.
          </p>
        </div>
      </header>

      <form className="lookup-form" onSubmit={handleLookup}>
        <label className="form-field">
          <span>Lookup by order ID</span>
          <input
            type="number"
            min={1}
            value={lookupId}
            onChange={(event) => setLookupId(event.currentTarget.value)}
            placeholder="e.g. 42"
          />
        </label>
        <button type="submit" className="btn ghost">
          Fetch
        </button>
      </form>

      {lookupStatus ? <div className="alert success">{lookupStatus}</div> : null}
      {lookupError ? <div className="alert error">{lookupError}</div> : null}

      {sortedOrders.length === 0 ? (
        <p className="muted">
          You do not have any tracked orders yet. Place an order from the
          catalogue or load an existing ID.
        </p>
      ) : (
        <ul className="order-list">
          {sortedOrders.map((order) => (
            <li key={order.orderId} className="order-card">
              <header>
                <strong>Order #{order.orderId}</strong>
                <span
                  className={`status-pill ${
                    order.status
                      ? `status-${order.status
                          .toLowerCase()
                          .replace(/[^a-z0-9]+/g, '-')}`
                      : 'status-unknown'
                  }`}
                >
                  {order.status ?? 'Unknown'}
                </span>
              </header>
              <dl>
                <div>
                  <dt>Product ID</dt>
                  <dd>{order.productId}</dd>
                </div>
                <div>
                  <dt>Quantity</dt>
                  <dd>{order.quantity}</dd>
                </div>
                <div>
                  <dt>Total</dt>
                  <dd>
                    {order.totalAmount !== undefined && order.totalAmount !== null
                      ? `$${order.totalAmount.toFixed(2)}`
                      : '—'}
                  </dd>
                </div>
                <div>
                  <dt>Bank transaction</dt>
                  <dd>{formatTransactionId(order.bankTransactionId)}</dd>
                </div>
                <div>
                  <dt>Updated</dt>
                  <dd>
                    {order.updatedAt
                      ? formatter.format(new Date(order.updatedAt))
                      : 'Unknown'}
                  </dd>
                </div>
              </dl>
              <footer>
                <button
                  type="button"
                  className="btn ghost"
                  onClick={() => fetchOrder(order.orderId)}
                >
                  Refresh
                </button>
                <button
                  type="button"
                  className="btn danger"
                  onClick={() => handleRefund(order)}
                  disabled={processingOrderId === order.orderId}
                >
                  {processingOrderId === order.orderId
                    ? 'Processing…'
                    : 'Request refund'}
                </button>
              </footer>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
};

export default OrdersPage;
