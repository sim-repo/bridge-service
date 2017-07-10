package com.simple.server.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


@Service("myService")
@Scope("singleton")
public class MyService extends AService {

}
