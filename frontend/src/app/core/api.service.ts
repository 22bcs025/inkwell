import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BlogResponse, CommentResponse, Page, UserResponse } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  getBlogs(page = 0, q?: string, sort = 'createdAt,desc') {
    let params = new HttpParams().set('page', page).set('size', 10).set('sort', sort);
    if (q) params = params.set('q', q);
    return this.http.get<Page<BlogResponse>>('/api/blogs', { params });
  }

  getTopBlogs(page = 0) {
    return this.http.get<Page<BlogResponse>>('/api/blogs/top-weekly', {
      params: new HttpParams().set('page', page).set('size', 10),
    });
  }

  getBlog(id: number)          { return this.http.get<BlogResponse>(`/api/blogs/${id}`); }
  createBlog(b: { title: string; content: string }) { return this.http.post<BlogResponse>('/api/blogs', b); }
  updateBlog(id: number, b: { title: string; content: string }) { return this.http.put<BlogResponse>(`/api/blogs/${id}`, b); }
  deleteBlog(id: number)       { return this.http.delete<void>(`/api/blogs/${id}`); }
  likeBlog(id: number)         { return this.http.post<BlogResponse>(`/api/blogs/${id}/like`, {}); }
  getLikeStatus(id: number) { return this.http.get<{liked: boolean}>(`/api/blogs/${id}/like`); }
  unlikeBlog(id: number)       { return this.http.delete<BlogResponse>(`/api/blogs/${id}/like`); }

  getComments(blogId: number, page = 0) {
    return this.http.get<Page<CommentResponse>>(`/api/blogs/${blogId}/comments`, {
      params: new HttpParams().set('page', page).set('size', 20),
    });
  }
  createComment(blogId: number, content: string) {
    return this.http.post<CommentResponse>(`/api/blogs/${blogId}/comments`, { content });
  }
  updateComment(id: number, content: string) {
    return this.http.put<CommentResponse>(`/api/comments/${id}`, { content });
  }
  deleteComment(id: number)    { return this.http.delete<void>(`/api/comments/${id}`); }

  getUser(id: number)          { return this.http.get<UserResponse>(`/api/users/${id}`); }
  updateUser(id: number, body: Partial<{ username: string; email: string; password: string }>) {
    return this.http.put<UserResponse>(`/api/users/${id}`, body);
  }
  deleteUser(id: number)       { return this.http.delete<void>(`/api/users/${id}`); }
}
