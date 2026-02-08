export interface Unit {
  id?: number;
  name: string;
  createdAt?: string;
  updatedAt?: string;
  deleted?: boolean;
  symbol: string;
}

export interface UnitResponseDTO {
  id: number;
  symbol: string;
}
