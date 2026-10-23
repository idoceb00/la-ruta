import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommunityCreateForm } from './community-create-form';

describe('CommunityCreateForm', () => {
  let component: CommunityCreateForm;
  let fixture: ComponentFixture<CommunityCreateForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommunityCreateForm],
    }).compileComponents();

    fixture = TestBed.createComponent(CommunityCreateForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
