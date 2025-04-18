package me.ddggdd135.slimeae.api;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class ConcurrentHashSet<E> implements Set<E> {
    private final ConcurrentHashMap<E, Boolean> map = new ConcurrentHashMap<>();

    @Override
    public boolean add(E e) {
        return map.put(e, Boolean.TRUE) == null;
    }

    @Override
    public boolean remove(Object e) {
        return map.remove(e) != null;
    }

    @Override
    public boolean contains(Object e) {
        return map.containsKey(e);
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c) {
            if (add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            E e = it.next();
            if (!c.contains(e)) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            if (remove(o)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Nonnull
    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator(); // 使用 ConcurrentHashMap 的 keySet 作为迭代器
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Nonnull
    @Override
    public <T> T[] toArray(@Nonnull T[] a) {
        return map.keySet().toArray(a);
    }

    public void clear() {
        map.clear();
    }

    @Nonnull
    public List<E> toList() {
        ArrayList<E> list = new ArrayList<>(size() + 4);

        list.addAll(this);

        return list;
    }
}
