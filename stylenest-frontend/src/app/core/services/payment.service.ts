import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE } from '../api.config';
import { Observable } from 'rxjs';

export interface PaymentRequest { orderId: string; amount: number; cardNumber: string; cvv: string; cardHolderName: string }
export interface PaymentResponse { paymentId: string; status: string }

@Injectable({ providedIn: 'root' })
export class PaymentService {
  constructor(private http: HttpClient) {}
  pay(req: PaymentRequest): Observable<PaymentResponse> { return this.http.post<PaymentResponse>(`${API_BASE}/api/payments`, req); }
}

