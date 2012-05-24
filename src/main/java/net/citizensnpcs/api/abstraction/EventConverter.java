package net.citizensnpcs.api.abstraction;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class EventConverter {
    private void convert() throws NotFoundException, CannotCompileException {
        ClassPool cp = ClassPool.getDefault();
        CtClass clazz = cp.get("");
        CtClass cEvent = cp.get("net.citizensnpcs.api.abstraction.Event");
        if (clazz.getName().equals("net.citizensnpcs.api.abstraction.Event")) {
            clazz.setSuperclass(cp.get("org.bukkit.event.Event"));
        } else {
            for (CtClass implement : clazz.getInterfaces()) {
                if (implement != cp.get("net.citizensnpcs.api.abstraction.Listener"))
                    continue;
                implement.addInterface(cp.get("org.bukkit.event.Listener"));
                return;
            }
            CtClass superClass = clazz;
            while ((superClass = superClass.getSuperclass()) != null) {
                if (superClass == cEvent)
                    break;
            }
            if (superClass == clazz || superClass == null)
                return;
            clazz.addField(CtField.make("private static final HanderList handlers = new HanderList();", clazz));
            CtMethod getHandlers = CtNewMethod.getter("getHandlerList", clazz.getField("handlers"));
            getHandlers.setModifiers(getHandlers.getModifiers() & Modifier.STATIC);
            clazz.addMethod(getHandlers);
            clazz.addMethod(CtNewMethod.getter("handlers", clazz.getField("handlers")));
        }
    }
}
