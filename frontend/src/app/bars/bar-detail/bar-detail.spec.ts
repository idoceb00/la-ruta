import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BarDetail } from './bar-detail';

describe('BarDetail', () => {
  let component: BarDetail;
  let fixture: ComponentFixture<BarDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BarDetail],
    }).compileComponents();

    fixture = TestBed.createComponent(BarDetail);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
