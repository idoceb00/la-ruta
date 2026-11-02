import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BarTapaAddForm } from './bar-tapa-add-form';

describe('BarTapaAddForm', () => {
  let component: BarTapaAddForm;
  let fixture: ComponentFixture<BarTapaAddForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BarTapaAddForm],
    }).compileComponents();

    fixture = TestBed.createComponent(BarTapaAddForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
