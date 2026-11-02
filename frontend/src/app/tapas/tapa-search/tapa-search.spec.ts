import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TapaSearch } from './tapa-search';

describe('TapaSearch', () => {
  let component: TapaSearch;
  let fixture: ComponentFixture<TapaSearch>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TapaSearch],
    }).compileComponents();

    fixture = TestBed.createComponent(TapaSearch);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
