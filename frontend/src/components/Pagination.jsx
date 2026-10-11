export default function Pagination({ data, onPage, noun = 'items' }) {
  if (!data || data.totalPages <= 1) return null;
  return (
    <div className="pagination">
      <span className="muted small">
        Page {data.page + 1} of {data.totalPages} · {data.totalElements} {noun}
      </span>
      <div className="btn-group">
        <button className="btn btn-outline btn-sm" disabled={data.page === 0} onClick={() => onPage(data.page - 1)}>
          Previous
        </button>
        <button className="btn btn-outline btn-sm" disabled={data.page + 1 >= data.totalPages} onClick={() => onPage(data.page + 1)}>
          Next
        </button>
      </div>
    </div>
  );
}
