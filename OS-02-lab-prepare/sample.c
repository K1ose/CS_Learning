// kern/mm/pmm.h

struct pmm_manager {
	const char *name;
	void (*init)(void);
	void (*init_memmap)(struct Page *base, size_t n);
	struct Page *(*alloc_pages)(size_t n);
	void (*free_pages)(struct Page *base, size_t n);
	size_t (*nr_free_pages)(void);
	void (*check)(void);
};

struct list_entry {
	struct list_entry *prev, *next;
};

typedef struct{
	list_entry_t free_list;
	unsigned int nr_free;
}free_erea_t;

struct Page{
	atomic_t ref;
	...
	list_entry_t page_link;
};