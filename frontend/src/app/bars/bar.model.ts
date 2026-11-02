export interface Bar {
  id: number;
  communityId: number;
  name: string;
  city: string | null;
  address: string | null;
  zone: string | null;
  averageRating: number | null;
  ratingCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface BarRequest {
  name: string;
  city: string | null;
  address: string | null;
  zone: string | null;
}

export interface MyBarReview {
  rating: number | null;
  notes: string | null;
}

export interface MyTapaReview {
  rating: number | null;
  notes: string | null;
  fav: boolean;
}

export interface BarTapaSummary {
  tapaId: number;
  tapaName: string;
  averageRating: number | null;
  ratingCount: number;
  myReview: MyTapaReview | null;
}

export interface BarDetailResponse extends Bar {
  myReview: MyBarReview | null;
  tapas: BarTapaSummary[];
}

export interface BarReviewRequest {
  rating: number | null;
  notes: string | null;
}