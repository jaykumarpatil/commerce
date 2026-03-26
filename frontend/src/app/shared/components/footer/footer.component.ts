import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  template: `
    <footer class="footer">
      <div class="container">
        <div class="footer-grid">
          <!-- Brand -->
          <div class="footer-brand">
            <a routerLink="/" class="logo">
              <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <path d="M16 10a4 4 0 0 1-8 0"></path>
              </svg>
              <span>Store</span>
            </a>
            <p class="footer-description">
              Your one-stop shop for premium products. Quality items, competitive prices, and excellent customer service.
            </p>
            <div class="social-links">
              <a href="#" aria-label="Facebook">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"></path>
                </svg>
              </a>
              <a href="#" aria-label="Twitter">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M23 3a10.9 10.9 0 0 1-3.14 1.53 4.48 4.48 0 0 0-7.86 3v1A10.66 10.66 0 0 1 3 4s-4 9 5 13a11.64 11.64 0 0 1-7 2c9 5 20 0 20-11.5a4.5 4.5 0 0 0-.08-.83A7.72 7.72 0 0 0 23 3z"></path>
                </svg>
              </a>
              <a href="#" aria-label="Instagram">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect>
                  <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path>
                  <line x1="17.5" y1="6.5" x2="17.51" y2="6.5"></line>
                </svg>
              </a>
            </div>
          </div>

          <!-- Quick Links -->
          <div class="footer-links">
            <h4>Quick Links</h4>
            <ul>
              <li><a routerLink="/products">All Products</a></li>
              <li><a routerLink="/products?featured=true">Featured</a></li>
              <li><a routerLink="/cart">Shopping Cart</a></li>
              <li><a routerLink="/orders">Track Order</a></li>
            </ul>
          </div>

          <!-- Customer Service -->
          <div class="footer-links">
            <h4>Customer Service</h4>
            <ul>
              <li><a href="#">Contact Us</a></li>
              <li><a href="#">FAQ</a></li>
              <li><a href="#">Shipping Info</a></li>
              <li><a href="#">Returns</a></li>
            </ul>
          </div>

          <!-- Newsletter -->
          <div class="footer-newsletter">
            <h4>Newsletter</h4>
            <p>Subscribe for exclusive offers and updates.</p>
            <form class="newsletter-form" (submit)="onSubscribe($event)">
              <input type="email" placeholder="Enter your email" required>
              <button type="submit">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="22" y1="2" x2="11" y2="13"></line>
                  <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                </svg>
              </button>
            </form>
          </div>
        </div>

        <div class="footer-bottom">
          <p>&copy; {{ currentYear }} Store. All rights reserved.</p>
          <div class="footer-legal">
            <a href="#">Privacy Policy</a>
            <a href="#">Terms of Service</a>
          </div>
        </div>
      </div>
    </footer>
  `,
  styles: [`
    .footer {
      background: var(--bg-secondary);
      border-top: 1px solid var(--border);
      padding: 4rem 0 2rem;
      margin-top: auto;
    }

    .footer-grid {
      display: grid;
      grid-template-columns: 2fr 1fr 1fr 2fr;
      gap: 3rem;
    }

    .footer-brand .logo {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: var(--text-primary);
      text-decoration: none;
      font-weight: 700;
      font-size: 1.25rem;
      margin-bottom: 1rem;
    }

    .logo-icon {
      width: 28px;
      height: 28px;
      color: var(--accent-primary);
    }

    .footer-description {
      color: var(--text-muted);
      font-size: 0.875rem;
      line-height: 1.6;
      margin-bottom: 1.5rem;
    }

    .social-links {
      display: flex;
      gap: 1rem;
    }

    .social-links a {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      background: var(--bg-tertiary);
      border-radius: 0.5rem;
      color: var(--text-secondary);
      transition: all 0.2s;
    }

    .social-links a:hover {
      background: var(--accent-primary);
      color: white;
    }

    .social-links svg {
      width: 20px;
      height: 20px;
    }

    .footer-links h4 {
      font-weight: 600;
      margin-bottom: 1.25rem;
    }

    .footer-links ul {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .footer-links a {
      color: var(--text-muted);
      text-decoration: none;
      font-size: 0.875rem;
      transition: color 0.2s;
    }

    .footer-links a:hover {
      color: var(--text-primary);
    }

    .footer-newsletter h4 {
      font-weight: 600;
      margin-bottom: 0.75rem;
    }

    .footer-newsletter p {
      color: var(--text-muted);
      font-size: 0.875rem;
      margin-bottom: 1rem;
    }

    .newsletter-form {
      display: flex;
      gap: 0.5rem;
    }

    .newsletter-form input {
      flex: 1;
      padding: 0.75rem 1rem;
      background: var(--bg-primary);
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      color: var(--text-primary);
      font-size: 0.875rem;
    }

    .newsletter-form input:focus {
      border-color: var(--accent-primary);
      outline: none;
    }

    .newsletter-form button {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0.75rem;
      background: var(--accent-primary);
      border-radius: 0.5rem;
      color: white;
      transition: all 0.2s;
    }

    .newsletter-form button:hover {
      background: var(--accent-primary-hover);
    }

    .newsletter-form svg {
      width: 20px;
      height: 20px;
    }

    .footer-bottom {
      margin-top: 3rem;
      padding-top: 2rem;
      border-top: 1px solid var(--border);
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .footer-bottom p {
      color: var(--text-muted);
      font-size: 0.875rem;
    }

    .footer-legal {
      display: flex;
      gap: 1.5rem;
    }

    .footer-legal a {
      color: var(--text-muted);
      text-decoration: none;
      font-size: 0.875rem;
      transition: color 0.2s;
    }

    .footer-legal a:hover {
      color: var(--text-primary);
    }

    @media (max-width: 1024px) {
      .footer-grid {
        grid-template-columns: 1fr 1fr;
      }
    }

    @media (max-width: 640px) {
      .footer-grid {
        grid-template-columns: 1fr;
      }

      .footer-bottom {
        flex-direction: column;
        gap: 1rem;
        text-align: center;
      }
    }
  `]
})
export class FooterComponent {
  currentYear = new Date().getFullYear();

  onSubscribe(event: Event): void {
    event.preventDefault();
    // Handle newsletter subscription
  }
}
