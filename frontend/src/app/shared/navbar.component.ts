import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <header class="sticky top-0 z-40 bg-white/90 backdrop-blur border-b border-slate-100">
      <nav class="max-w-4xl mx-auto px-4 h-14 flex items-center gap-4">
        <a routerLink="/" class="font-serif text-xl text-indigo-600 tracking-tight mr-auto">Inkwell</a>

        <a routerLink="/" routerLinkActive="text-slate-900 font-medium"
           [routerLinkActiveOptions]="{exact:true}"
           class="text-sm text-slate-500 hover:text-slate-900 transition-colors">Feed</a>

        @if (auth.isLoggedIn()) {
          <a routerLink="/write" class="btn-primary text-xs px-3 py-1.5">Write</a>
          <a [routerLink]="['/users', auth.currentUser()!.userId]"
             class="w-8 h-8 rounded-full bg-indigo-100 text-indigo-700 text-sm font-semibold flex items-center justify-center hover:bg-indigo-200 transition-colors"
             [title]="auth.currentUser()!.username">
            {{ auth.currentUser()!.username[0].toUpperCase() }}
          </a>
          <button (click)="auth.logout()" class="btn-ghost text-xs px-2">Sign out</button>
        } @else {
          <a routerLink="/login"  class="btn-ghost text-sm">Sign in</a>
          <a routerLink="/register" class="btn-primary text-sm">Join</a>
        }
      </nav>
    </header>
  `,
})
export class NavbarComponent {
  auth = inject(AuthService);
}
