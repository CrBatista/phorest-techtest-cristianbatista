import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ClientsService, ClientSummary } from './clients.service';
import { environment } from '../../../environments/environment';

describe('ClientsService', () => {
  let service: ClientsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ClientsService]
    });

    service = TestBed.inject(ClientsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('fetches total clients', () => {
    let total = 0;
    service.getTotalClients().subscribe(value => (total = value));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/clients?page=0&size=1`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 12 });

    expect(total).toBe(12);
  });

  it('returns a list of client summaries', () => {
    const mockClients: ClientSummary[] = [
      { id: 'c1', firstName: 'Jane', lastName: 'Doe', email: 'jane@example.com', phone: '123', gender: 'Female' }
    ];

    let clients: ClientSummary[] = [];
    service.getClients(3).subscribe(value => (clients = value));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/clients?page=0&size=3`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: mockClients, totalElements: 1 });

    expect(clients.length).toBe(1);
    expect(clients[0].id).toBe('c1');
  });

  it('fetches a client by id', () => {
    let client: ClientSummary | undefined;
    service.getClientById('c1').subscribe(value => (client = value));

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/clients/c1`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 'c1', firstName: 'Jane', lastName: 'Doe', email: 'jane@example.com', phone: '123', gender: 'Female' });

    expect(client?.id).toBe('c1');
  });
});
