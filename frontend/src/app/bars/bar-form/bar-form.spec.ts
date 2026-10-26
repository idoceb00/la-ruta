import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BarForm } from './bar-form';

describe('BarForm', () => {
  let component: BarForm;
  let fixture: ComponentFixture<BarForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BarForm],
    }).compileComponents();

    fixture = TestBed.createComponent(BarForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
