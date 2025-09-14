import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { Router } from '@angular/router';
import { Cart, CartItem, CartService } from '../../core/services/cart.service';
import { Product, ProductService } from '../../core/services/product.service';
import { OrderService } from '../../core/services/order.service';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';

@Component({
  standalone: true,
  selector: 'app-cart-page',
  imports: [CommonModule, CurrencyPipe, TableModule, ButtonModule],
  template: `
  <h2>Your Cart</h2>
  <div *ngIf="!cart || cart.items.length===0">Cart is empty</div>
  <p-table *ngIf="cart && cart.items.length>0" [value]="cart.items">
    <ng-template pTemplate="header">
      <tr><th>Product</th><th>Qty</th><th>Price</th><th>Total</th><th></th></tr>
    </ng-template>
    <ng-template pTemplate="body" let-i>
      <tr>
        <td>{{ productName(i.productId) }}</td>
        <td>{{i.quantity}}</td>
        <td>{{i.price | currency}}</td>
        <td>{{i.price * i.quantity | currency}}</td>
        <td><button pButton label="Remove" (click)="remove(i)"></button></td>
      </tr>
    </ng-template>
  </p-table>
  <p *ngIf="cart">Sum: <strong>{{sum(cart.items) | currency}}</strong></p>
  <div>
    <button pButton label="Clear" (click)="clear()"></button>
    <button pButton label="Place Order" [disabled]="!cart || cart.items.length===0" (click)="placeOrder()"></button>
  </div>
  <div *ngIf="orderId">Order created: #{{orderId}} <button pButton label="Pay now" (click)="goPay()"></button></div>
  `
})
export class CartPageComponent implements OnInit {
  cart!: Cart;
  orderId?: number;
  private products: Record<string, Product | undefined> = {};
  constructor(private cartSvc: CartService, private orders: OrderService, private router: Router, private productsSvc: ProductService) {}
  ngOnInit(){ this.load(); }
  load(){
    this.cartSvc.getCart().subscribe(c => {
      this.cart = c;
      const ids = Array.from(new Set((c.items || []).map(i => String(i.productId))));
      ids.forEach(pid => {
        if (!this.products[pid]) {
          const idNum = Number(pid);
          if (!isNaN(idNum)) {
            this.productsSvc.getProduct(idNum).subscribe({
              next: p => this.products[pid] = p,
              error: () => this.products[pid] = undefined
            });
          }
        }
      });
    });
  }
  productName(pid: string){ return this.products[pid]?.name ?? pid; }
  sum(items: CartItem[]){ return items.reduce((s,i)=> s + i.price * i.quantity, 0); }
  clear(){ this.cartSvc.clear().subscribe(()=> this.load()); }
  remove(i: CartItem){ this.cartSvc.removeItem(i.productId).subscribe(()=> this.load()); }
  placeOrder(){
    const items = this.cart.items.map(i => ({ productId: Number(i.productId), quantity: i.quantity, price: i.price }));
    this.orders.createOrder(items).subscribe(o => { this.orderId = o.id; });
  }
  goPay(){ if(this.orderId) this.router.navigate(['/payment', this.orderId]); }
}
