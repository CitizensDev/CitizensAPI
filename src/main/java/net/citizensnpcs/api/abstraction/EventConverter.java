/*
 * CitizensAPI
 * Copyright (C) 2012 CitizensDev <http://citizensnpcs.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.citizensnpcs.api.abstraction;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.Translator;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.EnumMemberValue;

public class EventConverter implements Translator {
    private void convertListener(ClassPool pool, CtClass clazz) throws NotFoundException {
        clazz.addInterface(pool.get("org.bukkit.event.Listener"));

        ConstPool constPool = clazz.getClassFile().getConstPool();

        for (CtMethod method : clazz.getMethods()) {
            if (!method.hasAnnotation(EventHandler.class))
                continue;
            Annotation eventHandler = new Annotation("org.bukkit.event.EventHandler", constPool);
            AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            attr.addAnnotation(eventHandler);

            EnumMemberValue value = new EnumMemberValue(constPool);
            value.setType("org.bukkit.event.Priority");
            EventHandler handler;
            try {
                handler = (EventHandler) method.getAnnotation(EventHandler.class);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            value.setValue(handler.priority().name());

            eventHandler.addMemberValue("priority", value);
            method.getMethodInfo().addAttribute(attr);
        }
    }

    private boolean isListener(CtClass listenerClass, CtClass clazz) throws NotFoundException {
        for (CtClass implement : clazz.getInterfaces()) {
            if (implement == listenerClass)
                return true;
        }
        return false;
    }

    @Override
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
    }

    @Override
    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
        CtClass clazz = pool.get(classname);
        CtClass cEvent = pool.get("net.citizensnpcs.api.abstraction.Event");
        if (isListener(pool.get("net.citizensnpcs.api.abstraction.Listener"), clazz)) {
            convertListener(pool, clazz);
            return;
        }
        CtClass superClass = clazz.getSuperclass(), last;
        while (true) {
            last = superClass;
            superClass = superClass.getSuperclass();
            if (superClass == null)
                return;
            else if (superClass != cEvent)
                continue;
            last.setSuperclass(pool.get("org.bukkit.event.Event"));
            addHandlerList(clazz);
            return;
        }
    }

    private void addHandlerList(CtClass clazz) throws NotFoundException, CannotCompileException {
        clazz.addField(CtField.make("private static final HanderList handlers = new HanderList();", clazz));
        CtMethod getHandlers = CtNewMethod.getter("getHandlerList", clazz.getField("handlers"));
        getHandlers.setModifiers(getHandlers.getModifiers() & Modifier.STATIC);
        clazz.addMethod(getHandlers);
        clazz.addMethod(CtNewMethod.getter("handlers", clazz.getField("handlers")));
    }
}
