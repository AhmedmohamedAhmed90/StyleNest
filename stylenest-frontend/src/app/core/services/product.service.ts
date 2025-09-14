import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE } from '../api.config';
import { Observable } from 'rxjs';

export interface Category { categoryId: number; name: string; description?: string }
export interface Product { productId: number; name: string; description?: string; price: number; stock: number; category?: Category }

@Injectable({ providedIn: 'root' })
export class ProductService {
  constructor(private http: HttpClient) {}

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${API_BASE}/api/categories`);
  }
  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${API_BASE}/api/products`);
  }
  getProduct(id: number): Observable<Product> {
    return this.http.get<Product>(`${API_BASE}/api/products/${id}`);
  }
}
