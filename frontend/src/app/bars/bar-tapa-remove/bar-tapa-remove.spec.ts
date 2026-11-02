import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BarTapaRemove } from './bar-tapa-remove';

describe('BarTapaRemove', () => {
  let component: BarTapaRemove;
  let fixture: ComponentFixture<BarTapaRemove>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BarTapaRemove],
    }).compileComponents();

    fixture = TestBed.createComponent(BarTapaRemove);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
