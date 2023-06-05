package com.marginallyclever.robotoverlord.parameters;

import com.fasterxml.jackson.databind.deser.impl.CreatorCandidate;
import com.marginallyclever.robotoverlord.SerializationContext;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * a list of parameters
 * @param <T> the type of parameter
 */
public class ListParameter<T extends AbstractParameter<?>> extends AbstractParameter<List<T>> {
    public ListParameter() {
        super();
        set(new ArrayList<>());
    }

    public ListParameter(String name, List<T> ts) {
        super(name, ts);
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        List<T> list = get();
        int size = list.size();
        jo.put("size", size);
        for(int i=0;i<size;++i) {
            jo.put("item"+i, list.get(i).toJSON(context));
        }
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) {
        super.parseJSON(jo, context);
        int size = jo.getInt("size");
        List<T> list = new ArrayList<>();
        for(int i=0;i<size;++i) {
            JSONObject item = jo.getJSONObject("item"+i);
            String type = item.getString("type");
            try {
                Class<?> c = Class.forName(type);
                T t = (T)c.getDeclaredConstructor().newInstance();
                t.parseJSON(item,context);
                list.add(t);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        set(list);
    }

    public void add(T referenceParameter) {
        get().add(referenceParameter);
    }

    public void clear() {
        get().clear();
    }

    public void remove(T referenceParameter) {
        get().remove(referenceParameter);
    }

    public T get(int index) {
        return get().get(index);
    }

    public int size() {
        return get().size();
    }
}
