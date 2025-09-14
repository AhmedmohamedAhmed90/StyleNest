import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../core/services/payment.service';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { ButtonModule } from 'primeng/button';
import { OrderService } from '../../core/services/order.service';
import { CartService } from '../../core/services/cart.service';

@Component({
  standalone: true,
  selector: 'app-payment-page',
  imports: [CommonModule, FormsModule, CardModule, InputTextModule, InputNumberModule, ButtonModule],
  template: `
  <p-card header="Payment for Order #{{orderId}}" [ngStyle]="{ 'max-width':'520px','margin':'1rem auto' }">
    <form (ngSubmit)="pay()" #f="ngForm">
      <div class="p-field"><label for="pay-amount">Amount</label><p-inputNumber [inputId]="'pay-amount'" [(ngModel)]="amount" name="amount" mode="currency" currency="USD" locale="en-US"></p-inputNumber></div>
      <div class="p-field"><label for="pay-card">Card Number</label><input id="pay-card" pInputText [(ngModel)]="cardNumber" name="cardNumber" required /></div>
      <div class="p-field"><label for="pay-cvv">CVV</label><input id="pay-cvv" pInputText [(ngModel)]="cvv" name="cvv" required /></div>
      <div class="p-field"><label for="pay-holder">Card Holder Name</label><input id="pay-holder" pInputText [(ngModel)]="cardHolderName" name="cardHolderName" required /></div>
      <button pButton label="Pay" [disabled]="f.invalid || loading" type="submit"></button>
      <div *ngIf="msg" style="margin-top:.5rem">{{msg}}</div>
    </form>
  </p-card>
  `
})
export class PaymentPageComponent {
  orderId = '';
  amount = 0;
  cardNumber = '4242424242424242';
  cvv = '123';
  cardHolderName = 'Test User';
  loading=false; msg='';
  constructor(private route: ActivatedRoute, private svc: PaymentService, private router: Router, private orders: OrderService, private cart: CartService) {
    this.orderId = String(this.route.snapshot.paramMap.get('orderId') || '');
    // Try prefill amount from order items
    this.orders.getOrders().subscribe(os => {
      const o = os.find(x => String(x.id) === this.orderId);
      if (o) this.amount = o.items.reduce((s,i)=> s + i.price*i.quantity, 0);
    });
  }
  pay(){
    this.loading=true; this.msg='';
    this.svc.pay({ orderId: this.orderId, amount: this.amount, cardNumber: this.cardNumber, cvv: this.cvv, cardHolderName: this.cardHolderName })
      .subscribe({ next: res => {
          this.msg = `Payment ${res.status}`; this.loading=false;
          if(res.status==='Succeeded'){
            // Clear cart after successful payment, then navigate
            this.cart.clear().subscribe({ complete: () => this.router.navigateByUrl('/orders') });
          }
        },
        error: err => { this.msg='Payment failed'; this.loading=false; console.error(err); }
      });
  }
}
