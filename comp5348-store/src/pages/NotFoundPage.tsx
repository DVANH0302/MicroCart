import { Link } from 'react-router-dom';

const NotFoundPage = () => (
  <section className="card narrow">
    <header className="card-header">
      <div>
        <h1>Page not found</h1>
        <p className="muted">
          The page you requested could not be located. Return to the catalogue to
          continue browsing the store.
        </p>
      </div>
    </header>
    <Link className="btn primary" to="/catalog">
      Go to catalogue
    </Link>
  </section>
);

export default NotFoundPage;
