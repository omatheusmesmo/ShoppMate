export interface Category {
  id?: number;
  name: string;
  createdAt?: string;
  updatedAt?: string;
  deleted?: boolean;
}

export interface CategoryResponseDTO {
  id: number;
  name: string;
}
