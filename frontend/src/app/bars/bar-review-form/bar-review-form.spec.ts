import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BarReviewForm } from './bar-review-form';

describe('BarReviewForm', () => {
  let component: BarReviewForm;
  let fixture: ComponentFixture<BarReviewForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BarReviewForm],
    }).compileComponents();

    fixture = TestBed.createComponent(BarReviewForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
