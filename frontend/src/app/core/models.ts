export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
}

export interface BlogResponse {
  id: number;
  title: string;
  content: string;
  authorId: number;
  authorUsername: string;
  createdAt: string;
  updatedAt: string;
  commentCount: number;
  views: number;
  likes: number;
}

export interface CommentResponse {
  id: number;
  content: string;
  blogId: number;
  authorId: number;
  authorUsername: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  createdAt: string;
}
