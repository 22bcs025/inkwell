import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { BlogResponse, Page } from '../../core/models';
import { BlogCardComponent } from '../../shared/blog-card.component';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';

type Tab = 'latest' | 'top';

@Component({
  selector: 'app-blog-list',
  standalone: true,
  imports: [FormsModule, BlogCardComponent],
  template: `
    <div class="max-w-4xl mx-auto px-4 py-8">

      <!-- header row -->
      <div class="flex items-center gap-3 mb-6 flex-wrap">
        <div class="flex bg-slate-100 rounded-lg p-0.5 text-sm">
          <button (click)="setTab('latest')"
            [class.bg-white]="tab()==='latest'" [class.shadow-sm]="tab()==='latest'"
            class="px-3 py-1.5 rounded-md font-medium transition-all">Latest</button>
          <button (click)="setTab('top')"
            [class.bg-white]="tab()==='top'" [class.shadow-sm]="tab()==='top'"
            class="px-3 py-1.5 rounded-md font-medium transition-all">Top</button>
        </div>

        @if (tab() === 'latest') {
          <div class="relative ml-auto w-64">
            <svg class="absolute left-2.5 top-2 w-4 h-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-4.35-4.35M17 11A6 6 0 115 11a6 6 0 0112 0z"/>
            </svg>
            <input [(ngModel)]="query" (ngModelChange)="search$.next($event)"
              class="input pl-8 py-1.5 text-sm" placeholder="Search posts…">
          </div>
        }
      </div>

      <!-- posts -->
      @if (loading()) {
        <div class="space-y-4">
          @for (_ of [1,2,3,4,5]; track $index) {
            <div class="card p-5 animate-pulse">
              <div class="h-5 bg-slate-100 rounded w-2/3 mb-2"></div>
              <div class="h-3 bg-slate-100 rounded w-full mb-1"></div>
              <div class="h-3 bg-slate-100 rounded w-4/5"></div>
            </div>
          }
        </div>
      } @else if (page()?.content?.length === 0) {
        <div class="text-center py-20 text-slate-400">
          <p class="font-serif text-2xl mb-2">Nothing here yet.</p>
          <p class="text-sm">Be the first to write something.</p>
        </div>
      } @else {
        <div class="space-y-4">
          @for (b of page()?.content; track b.id) {
            <app-blog-card [blog]="b" />
          }
        </div>

        <!-- pagination -->
        @if ((page()?.totalPages ?? 0) > 1) {
          <div class="flex items-center justify-center gap-2 mt-8">
            <button [disabled]="currentPage() === 0" (click)="goTo(currentPage()-1)" class="btn-ghost text-sm">← Prev</button>
            <span class="text-sm text-slate-500">{{ currentPage()+1 }} / {{ page()?.totalPages }}</span>
            <button [disabled]="currentPage() >= (page()?.totalPages ?? 1)-1" (click)="goTo(currentPage()+1)" class="btn-ghost text-sm">Next →</button>
          </div>
        }
      }
    </div>
  `,
})
export class BlogListComponent implements OnInit {
  private api = inject(ApiService);

  tab         = signal<Tab>('latest');
  loading     = signal(true);
  page        = signal<Page<BlogResponse> | null>(null);
  currentPage = signal(0);
  query       = '';
  search$     = new Subject<string>();

 ngOnInit() {
  this.load();
  this.search$.pipe(
    debounceTime(350),
    switchMap(q => {
      this.loading.set(true);
      this.currentPage.set(0);
      return this.api.getBlogs(0, q || undefined);
    }),
  ).subscribe(p => {
    this.page.set(p);
    this.currentPage.set(0);
    this.loading.set(false);
  });
}
  setTab(t: Tab) {
  this.tab.set(t);
  this.currentPage.set(0);
  this.query = '';
  this.search$.next('');
  this.load();
}

  goTo(n: number) { this.currentPage.set(n); this.load(); }

  private load() {
    this.loading.set(true);
    const obs = this.tab() === 'top'
      ? this.api.getTopBlogs(this.currentPage())
      : this.api.getBlogs(this.currentPage(), this.query || undefined);
    obs.subscribe(p => { this.page.set(p); this.loading.set(false); });
  }
}
