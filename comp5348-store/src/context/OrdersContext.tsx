import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import type { ReactNode } from 'react';
import apiClient from '../api/client';
import type { OrderResponse } from '../api/types';
import { useAuth } from './AuthContext';

type OrdersContextValue = {
  orders: OrderResponse[];
  addOrder: (order: OrderResponse) => void;
  fetchOrder: (orderId: number) => Promise<OrderResponse | null>;
  requestRefund: (orderId: number) => Promise<OrderResponse>;
  clearOrders: () => void;
  fetchOrdersForUser: (userId: number | string) => Promise<OrderResponse[]>;
};

const OrdersContext = createContext<OrdersContextValue | undefined>(undefined);

const storageKeyForUser = (username: string) =>
  `store-orders-${username.toLowerCase()}`;

const normaliseOrders = (orders: OrderResponse[]): OrderResponse[] =>
  orders
    .filter((order) => order?.orderId != null)
    .reduce<OrderResponse[]>((acc, current) => {
      if (acc.some((order) => order.orderId === current.orderId)) {
        return acc;
      }
      return [...acc, current];
    }, [])
    .sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''));

export const OrdersProvider = ({
  children,
}: {
  children: ReactNode;
}) => {
  const { user, token } = useAuth();
  const [orders, setOrders] = useState<OrderResponse[]>([]);

  useEffect(() => {
    if (!user) {
      setOrders([]);
      return;
    }
    try {
      const stored = localStorage.getItem(storageKeyForUser(user.username));
      if (stored) {
        const parsed = JSON.parse(stored) as OrderResponse[];
        setOrders(normaliseOrders(parsed));
      } else {
        setOrders([]);
      }
    } catch {
      setOrders([]);
    }
  }, [user]);

  useEffect(() => {
    if (!user) {
      return;
    }
    try {
      localStorage.setItem(
        storageKeyForUser(user.username),
        JSON.stringify(orders),
      );
    } catch {
      // ignore storage errors
    }
  }, [orders, user]);

  const addOrder = useCallback((order: OrderResponse) => {
    setOrders((prev) => normaliseOrders([order, ...prev]));
  }, []);

  const fetchOrder = useCallback(
    async (orderId: number): Promise<OrderResponse | null> => {
      try {
        const config = token
          ? { headers: { Authorization: `Bearer ${token}` } }
          : undefined;
        const { data } = await apiClient.get<OrderResponse>(
          `/api/orders/${orderId}`,
          config,
        );
        addOrder(data);
        return data;
      } catch (error) {
        return null;
      }
    },
    [addOrder, token],
  );

  const fetchOrdersForUser = useCallback(
    async (userId: number | string): Promise<OrderResponse[]> => {
      try {
        // debug: log whether we have a token at request time
        // (visible in browser console as `fetchOrdersForUser` logs)
        // Remove these logs after debugging in production
        // eslint-disable-next-line no-console
        console.debug('fetchOrdersForUser', { userId, hasToken: !!token });
        const config = token
          ? { headers: { Authorization: `Bearer ${token}` } }
          : undefined;
        const { data } = await apiClient.get<OrderResponse[]>(
          `/api/orders/users/${userId}`,
          config,
        );
        const normalised = normaliseOrders(data ?? []);
        setOrders(normalised);
        return normalised;
      } catch (error) {
        // log error for debugging then re-throw so callers can show UI errors
        // eslint-disable-next-line no-console
        console.error('fetchOrdersForUser failed', error);
        throw error;
      }
    },
    [token],
  );

  const requestRefund = useCallback(
    async (orderId: number): Promise<OrderResponse> => {
      const config = token
        ? { headers: { Authorization: `Bearer ${token}` } }
        : undefined;
      // POST with no body; axios expects either data or config as second param, so pass undefined
      const { data } = await apiClient.post<OrderResponse>(
        `/api/orders/${orderId}/refund`,
        undefined,
        config,
      );
      addOrder(data);
      return data;
    },
    [addOrder, token],
  );

  const clearOrders = useCallback(() => {
    setOrders([]);
  }, []);

  const value = useMemo(
    () => ({
      orders,
      addOrder,
      fetchOrder,
      fetchOrdersForUser,
      requestRefund,
      clearOrders,
    }),
    [orders, addOrder, fetchOrder, requestRefund, clearOrders, fetchOrdersForUser],
  );

  return (
    <OrdersContext.Provider value={value}>{children}</OrdersContext.Provider>
  );
};

export const useOrders = () => {
  const ctx = useContext(OrdersContext);
  if (!ctx) {
    throw new Error('useOrders must be used within OrdersProvider');
  }
  return ctx;
};
