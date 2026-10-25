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