import { CategoryResponseDTO } from './category.interface';
import { UnitResponseDTO } from './unit.interface';

export interface ItemRequestDTO {
  name: string;
  idCategory: number;
  idUnit: number;
}

export interface ItemResponseDTO {
  id: number;
  name: string;
  category: CategoryResponseDTO;
  unit: UnitResponseDTO;
}
