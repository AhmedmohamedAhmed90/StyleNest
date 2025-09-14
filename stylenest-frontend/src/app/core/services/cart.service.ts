import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE } from '../api.config';
import { Observable } from 'rxjs';

export interface CartItem { productId: string; quantity: number; price: number }
export interface Cart { userId: string; items: CartItem[] }

@Injectable({ providedIn: 'root' })
export class CartService {
  constructor(private http: HttpClient) {}
  getCart(): Observable<Cart> { return this.http.get<Cart>(`${API_BASE}/api/cart`); }
  addItem(item: CartItem): Observable<Cart> { return this.http.post<Cart>(`${API_BASE}/api/cart/items`, item); }
  addItems(items: CartItem[]): Observable<Cart> { return this.http.post<Cart>(`${API_BASE}/api/cart/items/bulk`, items); }
  updateQuantity(productId: string, quantity: number): Observable<Cart> { return this.http.put<Cart>(`${API_BASE}/api/cart/items/${productId}`, quantity); }
  removeItem(productId: string): Observable<Cart> { return this.http.delete<Cart>(`${API_BASE}/api/cart/items/${productId}`); }
  clear(): Observable<void> { return this.http.post<void>(`${API_BASE}/api/cart/clear`, {}); }
  checkout(): Observable<Cart> { return this.http.post<Cart>(`${API_BASE}/api/cart/checkout`, {}); }
}

