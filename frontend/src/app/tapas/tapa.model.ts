export interface Tapa {
  id: number;
  name: string;
}

export interface BarTapa {
  barId: number;
  barName: string;
  tapaId: number;
  tapaName: string;
  averageRating: number | null;
  ratingCount: number;
}

export interface BarTapaRequest {
  tapaName: string;
}

export interface TapaReviewRequest {
  rating: number | null;
  notes: string | null;
  fav: boolean;
}