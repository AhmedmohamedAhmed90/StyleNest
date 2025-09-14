import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE } from '../api.config';
import { Observable } from 'rxjs';

export interface OrderItemReq { productId: number; quantity: number; price: number }
export interface OrderItem extends OrderItemReq { id: number; reserved?: boolean }
export interface Order { id: number; userId: number; status: string; createdAt: string; items: OrderItem[] }

@Injectable({ providedIn: 'root' })
export class OrderService {
  constructor(private http: HttpClient) {}
  createOrder(items: OrderItemReq[]): Observable<Order> {
    return this.http.post<Order>(`${API_BASE}/api/orders`, items);
  }
  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${API_BASE}/api/orders`);
  }
}

