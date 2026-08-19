export interface Location {
  id: number;
  userId: number;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateLocationRequest {
  name: string;
}

export interface RenameLocationRequest {
  name: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  itemCount?: number;
  path: string;
}
