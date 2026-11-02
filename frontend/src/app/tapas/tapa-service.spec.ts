import { TestBed } from '@angular/core/testing';
import { TapaService } from './tapa-service';

describe('TapasService', () => {
  let service: TapaService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TapaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
