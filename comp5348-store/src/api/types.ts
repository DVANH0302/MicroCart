export interface LoginResponse {
  userId: number;
  username: string;
  email: string;
  accessToken: string;
  message: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  firstName: string;
  lastName: string;
  bankAccountId: string;
}

export interface RegisterResponse {
  userId: number;
  username: string;
  message: string;
}

export interface AvailabilityRequest {
  productId: number;
  quantity: number;
}

export interface AvailabilityResponse {
  canFulfill: boolean;
  allocations: Array<{
    warehouseId: number;
    available: number;
  }> | null;
}

export interface OrderRequest {
  username: string;
  productId: number;
  quantity: number;
  totalAmount: number;
}

export interface OrderResponse {
  orderId: number;
  username: string;
  productId: number;
  quantity: number;
  totalAmount: number;
  status: string;
  bankTransactionId?: string | null;
  warehouseIds?: number[] | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface RefundResponse extends OrderResponse {}

export interface ProductStockResponse {
  productId: number;
  productName: string;
  price: number;
  totalQuantity: number;
  warehouses: Array<{
    warehouseId: number;
    warehouseName: string;
    quantity: number;
  }>;
}
