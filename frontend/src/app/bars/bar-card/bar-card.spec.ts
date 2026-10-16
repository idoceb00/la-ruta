import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BarCard } from './bar-card';

describe('BarCard', () => {
  let component: BarCard;
  let fixture: ComponentFixture<BarCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BarCard],
    }).compileComponents();

    fixture = TestBed.createComponent(BarCard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
