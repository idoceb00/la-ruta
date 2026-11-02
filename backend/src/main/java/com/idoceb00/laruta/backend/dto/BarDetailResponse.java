package com.idoceb00.laruta.backend.dto;

import com.idoceb00.laruta.backend.model.Bar;
import com.idoceb00.laruta.backend.model.BarReview;
import com.idoceb00.laruta.backend.model.BarTapa;
import com.idoceb00.laruta.backend.model.TapaReview;

import java.time.Instant;
import java.util.List;

public record BarDetailResponse(
        Long id,
        Long communityId,
        String name,
        String city,
        String address,
        String zone,
        Double averageRating,
        long ratingCount,
        Instant createdAt,
        Instant updatedAt,
        MyBarReview myReview,
        List<TapaDetail> tapas
) {
    public static BarDetailResponse from(Bar bar, BarReview myReview, List<TapaDetail> tapas) {
        return new BarDetailResponse(
                bar.getId(),
                bar.getCommunity().getId(),
                bar.getName(),
                bar.getCity(),
                bar.getAddress(),
                bar.getZone(),
                bar.getAverageRating(),
                bar.getRatingCount(),
                bar.getCreatedAt(),
                bar.getUpdatedAt(),
                myReview == null ? null : MyBarReview.from(myReview),
                tapas
        );
    }

    public record MyBarReview(Integer rating, String notes) {
        static MyBarReview from(BarReview review) {
            return new MyBarReview(review.getRating(), review.getNotes());
        }
    }

    public record TapaDetail(
            Long tapaId,
            String tapaName,
            Double averageRating,
            long ratingCount,
            MyTapaReview myReview
    ) {
        public static TapaDetail from(BarTapa barTapa, TapaReview myReview) {
            return new TapaDetail(
                    barTapa.getTapa().getId(),
                    barTapa.getTapa().getName(),
                    barTapa.getAverageRating(),
                    barTapa.getRatingCount(),
                    myReview == null ? null : MyTapaReview.from(myReview)
            );
        }
    }

    public record MyTapaReview(Integer rating, String notes, boolean fav) {
        static MyTapaReview from(TapaReview review) {
            return new MyTapaReview(review.getRating(), review.getNotes(), review.isFav());
        }
    }
}