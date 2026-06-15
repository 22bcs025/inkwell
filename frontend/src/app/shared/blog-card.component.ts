import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BlogResponse } from '../core/models';
import { TimeAgoPipe } from './time.pipe';

@Component({
  selector: 'app-blog-card',
  standalone: true,
  imports: [RouterLink, TimeAgoPipe],
  template: `
    <article class="card p-5 hover:shadow-md transition-shadow">
      <a [routerLink]="['/blogs', blog().id]" class="group block">
        <h2 class="font-serif text-xl text-slate-900 group-hover:text-indigo-600 transition-colors leading-snug mb-1">
          {{ blog().title }}
        </h2>
        <p class="text-sm text-slate-500 line-clamp-2 leading-relaxed">
          {{ plainText(blog().content) }}
        </p>
      </a>
      <footer class="mt-3 flex items-center gap-3 text-xs text-slate-400 flex-wrap">
        <a [routerLink]="['/users', blog().authorId]"
           class="font-medium text-slate-600 hover:text-indigo-600 transition-colors">
          {{ blog().authorUsername }}
        </a>
        <span>{{ blog().createdAt | timeAgo }}</span>
        <span class="ml-auto flex items-center gap-3">
          <span class="stat-pill" title="Views">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
            </svg>
            {{ blog().views }}
          </span>
          <span class="stat-pill" title="Likes">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
            </svg>
            {{ blog().likes }}
          </span>
          <span class="stat-pill" title="Comments">
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
            </svg>
            {{ blog().commentCount }}
          </span>
        </span>
      </footer>
    </article>
  `,
})
export class BlogCardComponent {
  blog = input.required<BlogResponse>();

  plainText(md: string) {
    return md.replace(/[#*`_\[\]()>~]/g, '').slice(0, 200);
  }
}
