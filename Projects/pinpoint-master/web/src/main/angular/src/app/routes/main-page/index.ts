import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from 'app/shared';
import { NoticeModule } from 'app/core/components/notice';
import { ApplicationListModule } from 'app/core/components/application-list';
import { ServerMapOptionsModule } from 'app/core/components/server-map-options';
import { PeriodSelectorModule } from 'app/core/components/period-selector';
import { TransactionIdSearchModule} from 'app/core/components/transaction-id-search';
import { ConfigurationIconModule } from 'app/core/components/configuration-icon';
import { MainContentsModule } from 'app/core/components/main-contents';
import { SideBarModule } from 'app/core/components/side-bar';
import { MainPageComponent } from './main-page.component';
import { routing } from './main-page.routing';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        MainPageComponent
    ],
    imports: [
        SharedModule,
        NoticeModule,
        ApplicationListModule,
        ServerMapOptionsModule,
        PeriodSelectorModule,
        TransactionIdSearchModule,
        ConfigurationIconModule,
        MainContentsModule,
        SideBarModule,
        HelpViewerPopupModule,
        RouterModule.forChild(routing)
    ],
    exports: [],
    providers: []
})
export class MainPageModule { }
