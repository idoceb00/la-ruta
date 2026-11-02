import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TapaReviewForm } from './tapa-review-form';

describe('TapaReviewForm', () => {
  let component: TapaReviewForm;
  let fixture: ComponentFixture<TapaReviewForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TapaReviewForm],
    }).compileComponents();

    fixture = TestBed.createComponent(TapaReviewForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
