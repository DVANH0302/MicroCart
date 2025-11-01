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
  const { user } = useAuth();
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
        const { data } = await apiClient.get<OrderResponse>(
          `/api/orders/${orderId}`,
        );
        addOrder(data);
        return data;
      } catch (error) {
        return null;
      }
    },
    [addOrder],
  );

  const requestRefund = useCallback(
    async (orderId: number): Promise<OrderResponse> => {
      const { data } = await apiClient.post<OrderResponse>(
        `/api/orders/${orderId}/refund`,
      );
      addOrder(data);
      return data;
    },
    [addOrder],
  );

  const clearOrders = useCallback(() => {
    setOrders([]);
  }, []);

  const value = useMemo(
    () => ({
      orders,
      addOrder,
      fetchOrder,
      requestRefund,
      clearOrders,
    }),
    [orders, addOrder, fetchOrder, requestRefund, clearOrders],
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
