import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="admin-page">
      <div class="container">
        <h1>Admin Dashboard</h1>
        <p class="coming-soon">Admin features coming soon...</p>
        
        <div class="admin-grid">
          <div class="admin-card">
            <h3>Products</h3>
            <p>Manage product catalog, pricing, and inventory</p>
          </div>
          <div class="admin-card">
            <h3>Orders</h3>
            <p>View and manage customer orders</p>
          </div>
          <div class="admin-card">
            <h3>Customers</h3>
            <p>Manage customer accounts and permissions</p>
          </div>
          <div class="admin-card">
            <h3>Analytics</h3>
            <p>View sales reports and metrics</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-page {
      padding: 2rem 0;
    }

    .admin-page h1 {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 2rem;
    }

    .coming-soon {
      color: var(--text-muted);
      margin-bottom: 2rem;
    }

    .admin-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1.5rem;
    }

    .admin-card {
      padding: 2rem;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      transition: all 0.2s;
    }

    .admin-card:hover {
      border-color: var(--accent-primary);
      transform: translateY(-2px);
    }

    .admin-card h3 {
      font-size: 1.125rem;
      font-weight: 600;
      margin-bottom: 0.5rem;
    }

    .admin-card p {
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    @media (max-width: 1024px) {
      .admin-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }

    @media (max-width: 640px) {
      .admin-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class AdminComponent {}
