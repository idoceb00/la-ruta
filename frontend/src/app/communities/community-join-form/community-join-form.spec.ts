import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommunityJoinForm } from './community-join-form';

describe('CommunityJoinForm', () => {
  let component: CommunityJoinForm;
  let fixture: ComponentFixture<CommunityJoinForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommunityJoinForm],
    }).compileComponents();

    fixture = TestBed.createComponent(CommunityJoinForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
