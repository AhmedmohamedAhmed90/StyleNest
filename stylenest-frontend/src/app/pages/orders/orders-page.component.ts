import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Order, OrderService } from '../../core/services/order.service';
import { TableModule } from 'primeng/table';

@Component({
  standalone: true,
  selector: 'app-orders-page',
  imports: [CommonModule, CurrencyPipe, DatePipe, TableModule],
  template: `
  <h2>Your Orders</h2>
  <p-table *ngIf="orders.length>0" [value]="orders">
    <ng-template pTemplate="header">
      <tr><th>#</th><th>Status</th><th>Created</th><th>Items</th><th>Total</th></tr>
    </ng-template>
    <ng-template pTemplate="body" let-o>
      <tr>
        <td>{{o.id}}</td>
        <td>{{o.status}}</td>
        <td>{{o.createdAt | date:'short'}}</td>
        <td>{{countItems(o)}}</td>
        <td>{{total(o) | currency}}</td>
      </tr>
    </ng-template>
  </p-table>
  <div *ngIf="orders.length===0">No orders yet.</div>
  `
})
export class OrdersPageComponent implements OnInit {
  orders: Order[] = [];
  constructor(private svc: OrderService) {}
  ngOnInit(){ this.svc.getOrders().subscribe(os => this.orders = os); }
  total(o: Order){ return o.items.reduce((s,i)=> s + i.price * i.quantity, 0); }
  countItems(o: Order){ return o.items.reduce((s,i)=> s + i.quantity, 0); }
}
