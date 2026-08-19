export interface Category {
  id: number;
  name: string;
  itemCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCategoryRequest {
  name: string;
}

export interface RenameCategoryRequest {
  name: string;
}
