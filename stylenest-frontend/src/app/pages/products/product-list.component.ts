import { Component, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product, ProductService } from '../../core/services/product.service';
import { CartService } from '../../core/services/cart.service';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';

@Component({
  standalone: true,
  selector: 'app-product-list',
  imports: [CommonModule, FormsModule, CurrencyPipe, CardModule, ButtonModule, InputNumberModule],
  template: `
  <h2>Products</h2>
  <div class="grid">
    <p-card *ngFor="let p of products" header="{{p.name}}" subheader="{{p.category?.name || ''}}">
      <ng-template pTemplate="content">
        <p>{{p.description}}</p>
        <p><strong>{{p.price | currency}}</strong></p>
        <div class="actions">
          <p-inputNumber [(ngModel)]="qty[p.productId]" [min]="1" [showButtons]="true" [inputId]="'qty-' + p.productId"></p-inputNumber>
          <button pButton label="Add to cart" (click)="addToCart(p)"></button>
        </div>
      </ng-template>
    </p-card>
  </div>
  `,
  styles: [`
    .grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(260px,1fr));gap:1rem}
    .actions{display:flex;gap:.5rem;align-items:center}
  `]
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  qty: Record<number, number> = {};
  constructor(private productsSvc: ProductService, private cart: CartService) {}
  ngOnInit(){ this.productsSvc.getProducts().subscribe(ps => { this.products = ps; ps.forEach(p=>this.qty[p.productId]=1); }); }
  addToCart(p: Product){ this.cart.addItem({ productId: String(p.productId), quantity: this.qty[p.productId]||1, price: p.price }).subscribe(); }
}
