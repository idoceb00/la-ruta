import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BarTapaList } from './bar-tapa-list';

describe('BarTapaList', () => {
  let component: BarTapaList;
  let fixture: ComponentFixture<BarTapaList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BarTapaList],
    }).compileComponents();

    fixture = TestBed.createComponent(BarTapaList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
