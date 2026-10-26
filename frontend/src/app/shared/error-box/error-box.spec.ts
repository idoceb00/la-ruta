import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ErrorBox } from './error-box';

describe('ErrorBox', () => {
  let component: ErrorBox;
  let fixture: ComponentFixture<ErrorBox>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ErrorBox],
    }).compileComponents();

    fixture = TestBed.createComponent(ErrorBox);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
